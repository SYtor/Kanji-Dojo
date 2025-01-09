package ua.syt0r.kanji.core.sync

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.AccountManager
import ua.syt0r.kanji.core.AccountState
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.SubscriptionInfo
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.use_case.HandleSyncIntentUseCase

interface SyncManager {
    val state: StateFlow<SyncFeatureState>
}

class DefaultSyncManager(
    private val accountManager: AccountManager,
    private val handleSyncIntentUseCase: HandleSyncIntentUseCase,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SyncManager {

    private val workerScope = CoroutineScope(dispatcher)

    private val _state = MutableStateFlow<SyncFeatureState>(SyncFeatureState.Loading)
    override val state: StateFlow<SyncFeatureState> = _state

    init {
        workerScope.launch { handleStateUpdates() }
    }

    private suspend fun handleStateUpdates() {
        syncFeatureStateFlow().collectLatest { syncFeatureState ->
            Logger.d("syncFeatureState[$syncFeatureState]")
            _state.value = syncFeatureState
        }
    }

    private fun syncFeatureStateFlow(): Flow<SyncFeatureState> {
        return accountManager.state.transformLatest { accountState ->
            when (accountState) {
                AccountState.LoggedOut,
                is AccountState.Error -> {
                    emit(SyncFeatureState.Disabled)
                }

                AccountState.Loading -> {
                    emit(SyncFeatureState.Loading)
                }

                is AccountState.LoggedIn -> when (accountState.subscriptionInfo) {
                    SubscriptionInfo.Inactive,
                    is SubscriptionInfo.Expired -> {
                        emit(SyncFeatureState.Disabled)
                    }

                    is SubscriptionInfo.Active -> coroutineScope {
                        val enabledState = SyncEnabledState(
                            coroutineScope = this,
                            handleSyncIntentUseCase = handleSyncIntentUseCase,
                            accountManager = accountManager,
                            initialStateIssue = accountState.issue
                        )
                        emit(enabledState)
                    }
                }
            }
        }
    }


}

private class SyncEnabledState(
    private val coroutineScope: CoroutineScope,
    private val handleSyncIntentUseCase: HandleSyncIntentUseCase,
    private val accountManager: AccountManager,
    initialStateIssue: ApiRequestIssue?
) : SyncFeatureState.Enabled {

    private data class IntentData(
        val intent: SyncIntent,
        val result: CompletableDeferred<SyncState>? = null
    )

    private val intentJobChannel = Channel<IntentData>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val _state = MutableStateFlow<SyncState>(
        value = initialStateIssue?.let { SyncState.Error.Api(it) } ?: SyncState.Refreshing
    )
    override val state: StateFlow<SyncState> = _state

    init {
        coroutineScope.launch {
            intentJobChannel.consumeAsFlow().collectLatest {
                val last = handleSyncIntentUseCase(coroutineScope, it.intent)
                    .onEach { syncState ->
                        syncState.updateAppState()
                        _state.value = syncState
                    }
                    .last()

                it.result?.complete(last)
            }
        }
        if (initialStateIssue == null) {
            coroutineScope.launch { intentJobChannel.send(IntentData(SyncIntent.Refresh)) }
        }
    }

    override fun sync(): CompletableDeferred<SyncState> {
        return handleIntent(SyncIntent.Sync)
    }

    override fun resolveConflict(strategy: SyncConflictResolveStrategy) {
        handleIntent(SyncIntent.ResolveConflict(strategy))
    }

    override fun cancel() {
        handleIntent(SyncIntent.Cancel)
    }

    private fun handleIntent(intent: SyncIntent): CompletableDeferred<SyncState> {
        val completableDeferred = CompletableDeferred<SyncState>()
        coroutineScope.launch { intentJobChannel.send(IntentData(intent, completableDeferred)) }
        return completableDeferred
    }

    private fun SyncState.updateAppState() {
        when (this) {
            is SyncState.Error.Api -> {
                when (issue) {
                    ApiRequestIssue.NotAuthenticated -> accountManager.invalidateAuth()
                    ApiRequestIssue.NoSubscription -> accountManager.invalidateSubscription()
                    else -> Unit
                }
            }

            else -> Unit
        }
    }

}
