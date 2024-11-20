package ua.syt0r.kanji.core.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.use_case.ApplyRemoteSyncDataUseCase
import ua.syt0r.kanji.core.sync.use_case.RefreshSyncStateUseCase
import ua.syt0r.kanji.core.sync.use_case.SubscribeOnSyncDataChangeUseCase
import ua.syt0r.kanji.core.sync.use_case.UploadSyncDataUseCase
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import kotlin.coroutines.CoroutineContext

interface SyncManager {
    val state: StateFlow<SyncState>
    fun refreshState()
    fun forceSync()
    fun cancelSync()
    fun resolveConflict(strategy: SyncConflictResolveStrategy)
}

class DefaultSyncManager(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val subscribeOnSyncDataChangeUseCase: SubscribeOnSyncDataChangeUseCase,
    private val refreshSyncStateUseCase: RefreshSyncStateUseCase,
    private val uploadSyncDataUseCase: UploadSyncDataUseCase,
    private val applyRemoteSyncDataUseCase: ApplyRemoteSyncDataUseCase,
    syncContext: CoroutineContext = Dispatchers.IO
) : SyncManager {

    private sealed interface ManagerAction {
        object Refresh : ManagerAction
        object Sync : ManagerAction
        object Cancel : ManagerAction
        data class ResolveConflict(val strategy: SyncConflictResolveStrategy) : ManagerAction
    }

    private val coroutineScope = CoroutineScope(syncContext)
    private val managerActionFlow = Channel<ManagerAction>()

    private val isDataChangedSinceLastSync = MutableStateFlow(false)

    private val _state = MutableStateFlow<SyncState>(SyncState.Loading.Refreshing)
    override val state: StateFlow<SyncState> = _state

    init {
        managerActionFlow.consumeAsFlow()
            .flatMapLatest(::executeAction)
            .onEach { _state.value = it }
            .launchIn(coroutineScope)

        appPreferences.syncEnabled.onModified
            .onEach { refreshState() }
            .launchIn(coroutineScope)

        subscribeOnSyncDataChangeUseCase(state)
            .onEach { isDataChangedSinceLastSync.value = it }
            .launchIn(coroutineScope)

        refreshState()
    }

    override fun refreshState() {
        coroutineScope.launch { managerActionFlow.send(ManagerAction.Refresh) }
    }

    override fun forceSync() {
        coroutineScope.launch { managerActionFlow.send(ManagerAction.Sync) }
    }

    override fun cancelSync() {
        coroutineScope.launch { managerActionFlow.send(ManagerAction.Cancel) }
    }

    override fun resolveConflict(strategy: SyncConflictResolveStrategy) {
        coroutineScope.launch { managerActionFlow.send(ManagerAction.ResolveConflict(strategy)) }
    }

    private fun executeAction(action: ManagerAction): Flow<SyncState> {
        return when (action) {
            ManagerAction.Cancel -> flowOf(SyncState.Canceled)

            ManagerAction.Refresh -> flow {
                emit(SyncState.Loading.Refreshing)
                Logger.d("Refresh sync, refreshing")
                val refreshResultState = refreshSyncStateUseCase()
                Logger.d("Refresh sync, refreshResultState[$refreshResultState]")
                emit(refreshResultState)
            }

            ManagerAction.Sync -> flow {
                emit(SyncState.Loading.Uploading)
                Logger.d("Uploading sync, refreshing")
                val refreshResultState = refreshSyncStateUseCase()
                Logger.d("Uploading sync, refreshResultState[$refreshResultState]")

                if (refreshResultState != SyncState.Enabled.PendingUpload) {
                    emit(refreshResultState)
                    return@flow
                }

                Logger.d("Uploading sync, starting upload")
                val uploadResultState = uploadSyncDataUseCase()
                Logger.d("Uploading sync, uploadResultState[$uploadResultState]")
                emit(uploadResultState)
            }

            is ManagerAction.ResolveConflict -> flow {
                when (action.strategy) {
                    SyncConflictResolveStrategy.UploadLocal -> {
                        Logger.d("Uploading sync, starting upload")
                        emit(SyncState.Loading.Uploading)
                        val uploadResultState = uploadSyncDataUseCase()
                        Logger.d("Uploading sync, uploadResultState[$uploadResultState]")
                        emit(uploadResultState)
                    }

                    SyncConflictResolveStrategy.DownloadRemote -> {
                        Logger.d("Downloading remote")
                        emit(SyncState.Loading.Downloading)
                        val applyRemoteSyncDataResult = applyRemoteSyncDataUseCase()
                        Logger.d("applyRemoteSyncDataResult[$applyRemoteSyncDataResult]")
                        emit(applyRemoteSyncDataResult)
                    }
                }
            }
        }
    }

}
