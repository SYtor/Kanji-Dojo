package ua.syt0r.kanji.core.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.AccountManager
import ua.syt0r.kanji.core.AccountState
import ua.syt0r.kanji.core.SubscriptionInfo
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.use_case.HandleSyncIntentUseCase
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface SyncManager {
    val state: StateFlow<SyncFeatureState>
    fun refresh()
    fun sync(): Flow<SyncState>
    fun cancel()
    fun resolveConflict(strategy: SyncConflictResolveStrategy)
}

class DefaultSyncManager(
    private val accountManager: AccountManager,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val handleSyncIntentUseCase: HandleSyncIntentUseCase,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SyncManager {

    private val intentHandlingSchedulerScope = CoroutineScope(dispatcher.limitedParallelism(1))
    private val workerScope = CoroutineScope(dispatcher)

    private var syncJob: Job? = null

    private val _state = MutableStateFlow<SyncFeatureState>(SyncFeatureState.Loading)
    override val state: StateFlow<SyncFeatureState> = _state

    init {
        workerScope.launch { handleStateUpdates() }
    }

    override fun refresh() {
        handleIntent(SyncIntent.Refresh)
    }

    override fun sync(): Flow<SyncState> {
        return handleIntent(SyncIntent.Sync)
    }

    override fun cancel() {
        intentHandlingSchedulerScope.launch {
            syncJob?.cancelAndJoin()
            val state = _state.value as MutableEnabledSyncFeatureState
            state.setState(SyncState.Canceled)
        }
    }

    override fun resolveConflict(strategy: SyncConflictResolveStrategy) {
        handleIntent(SyncIntent.ResolveConflict(strategy))
    }

    private suspend fun handleStateUpdates() {
        syncFeatureStateFlow().collectLatest { syncFeatureState ->
            Logger.d("syncFeatureState[$syncFeatureState]")
            _state.value = syncFeatureState

            if (syncFeatureState is MutableEnabledSyncFeatureState) {
                syncJob = workerScope.launch {
                    handleSyncIntentUseCase(workerScope, SyncIntent.Refresh)
                        .onEach {
                            Logger.d("syncState[$it]")
                            syncFeatureState.setState(it)
                        }
                        .collect()
                    Logger.d("Initial refresh complete")
                }.also {
                    try {
                        it.join()
                    } catch (e: CancellationException) {
                        it.cancelAndJoin()
                    }
                }
            }
        }
    }

    private fun handleIntent(intent: SyncIntent): Flow<SyncState> {
        val observableStateChannel = Channel<SyncState>(Channel.CONFLATED)

        intentHandlingSchedulerScope.launch {
            val enabledSyncState = _state.value as MutableEnabledSyncFeatureState

            val currentJob = syncJob
            currentJob?.cancelAndJoin()

            val updateState: suspend (SyncState) -> Unit = {
                Logger.d("syncState[$it]")
                enabledSyncState.setState(it)
                observableStateChannel.send(it)
            }

            syncJob = workerScope.launch {

                try {
                    handleSyncIntentUseCase(workerScope, intent)
                        .onEach(updateState)
                        .collect()
                } catch (e: CancellationException) {
                    updateState(SyncState.Canceled)
                }

                observableStateChannel.close()

            }

        }

        return observableStateChannel.consumeAsFlow()
    }

    private fun syncFeatureStateFlow(): Flow<SyncFeatureState> {
        return accountManager.state.flatMapLatest { accountState ->
            when (accountState) {
                AccountState.Loading -> flowOf(SyncFeatureState.Loading)
                is AccountState.Error -> flowOf(SyncFeatureState.Error(accountState.issue))
                AccountState.LoggedOut -> flowOf(SyncFeatureState.Disabled)
                is AccountState.LoggedIn -> when (accountState.subscriptionInfo) {
                    is SubscriptionInfo.Expired,
                    SubscriptionInfo.Inactive -> flowOf(SyncFeatureState.Disabled)

                    is SubscriptionInfo.Active -> activeSubscriptionStateFlow()
                }
            }
        }
    }

    private fun activeSubscriptionStateFlow(): Flow<SyncFeatureState> {
        return appPreferences.syncEnabled.onModified
            .onSubscription { emit(appPreferences.syncEnabled.get()) }
            .map { syncEnabled ->
                if (syncEnabled) {
                    MutableEnabledSyncFeatureState()
                } else {
                    SyncFeatureState.Disabled
                }
            }
    }

}
