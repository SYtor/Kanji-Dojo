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
import ua.syt0r.kanji.core.sync.use_case.GetRemoteBackupUseCase
import ua.syt0r.kanji.core.sync.use_case.RefreshSyncStateUseCase
import ua.syt0r.kanji.core.sync.use_case.SubscribeOnSyncDataChangeUseCase
import ua.syt0r.kanji.core.sync.use_case.UploadBackupUseCase
import kotlin.coroutines.CoroutineContext

interface SyncManager {
    val state: StateFlow<SyncState>
    fun refreshState()
    fun forceSync()
    fun cancelSync()
    fun resolveConflict(strategy: SyncConflictResolveStrategy)
}

class DefaultSyncManager(
    subscribeOnSyncDataChangeUseCase: SubscribeOnSyncDataChangeUseCase,
    private val refreshSyncStateUseCase: RefreshSyncStateUseCase,
    private val uploadBackupUseCase: UploadBackupUseCase,
    private val getRemoteBackupUseCase: GetRemoteBackupUseCase,
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

    private val _state = MutableStateFlow<SyncState>(SyncState.Refreshing)
    override val state: StateFlow<SyncState> = _state

    init {
        managerActionFlow.consumeAsFlow()
            .flatMapLatest(::executeAction)
            .onEach { _state.value = it }
            .launchIn(coroutineScope)

        subscribeOnSyncDataChangeUseCase()
            .onEach {  }
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
                emit(SyncState.Refreshing)
                emit(refreshSyncStateUseCase().also { Logger.d("refreshState[$it]") })
            }

            ManagerAction.Sync -> flow {
                emit(SyncState.Syncing)
                val refreshState = refreshSyncStateUseCase().also { Logger.d("refreshState[$it]") }
                if (refreshState == SyncState.PendingUpload) emit(uploadBackupUseCase())
                else emit(refreshState)
            }

            is ManagerAction.ResolveConflict -> flow {
                emit(SyncState.Syncing)
                when(action.strategy) {
                    SyncConflictResolveStrategy.UploadLocal -> {
                        emit(uploadBackupUseCase())
                    }
                    SyncConflictResolveStrategy.DownloadRemote -> TODO()
                }
            }
        }
    }

}
