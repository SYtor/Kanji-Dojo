package ua.syt0r.kanji.core.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.backup.BackupManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import kotlin.coroutines.CoroutineContext

interface SyncManager {
    val state: StateFlow<SyncState>
    fun forceSync()
    fun cancelSync()
}

sealed interface SyncState {
    object Preparing : SyncState
    object Disabled : SyncState
    object NoChanges : SyncState
    object SyncAvailable : SyncState
    object Syncing : SyncState
    object Canceled : SyncState
    object Fail : SyncState
}

class DefaultSyncManager(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val backupManager: BackupManager,
    syncContext: CoroutineContext = Dispatchers.IO
) : SyncManager {

    private val coroutineScope = CoroutineScope(syncContext)

    private val _state = MutableStateFlow<SyncState>(SyncState.Preparing)
    override val state: StateFlow<SyncState> = _state

    init {
        coroutineScope.launch {
            val syncEnabled = true // TODO
            _state.value = SyncState.SyncAvailable
        }
    }

    override fun forceSync() {
        _state.value = SyncState.Syncing
        coroutineScope.launch {
            delay(2000)
            _state.value = SyncState.NoChanges
        }
    }

    override fun cancelSync() {
        _state.value = SyncState.Canceled
    }

}
