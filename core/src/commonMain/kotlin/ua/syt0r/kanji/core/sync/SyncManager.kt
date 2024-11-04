package ua.syt0r.kanji.core.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
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

    data class Conflict(
        val remoteDataTimestamp: Instant,
        val localDataTimestamp: Instant
    ) : SyncState

    object Canceled : SyncState

    object AuthExpired : SyncState
    object MissingConnection : SyncState

    data class Fail(val throwable: Throwable) : SyncState

}

class DefaultSyncManager(
    private val userPreferencesRepository: UserPreferencesRepository,
    syncContext: CoroutineContext = Dispatchers.IO
) : SyncManager {

    val coroutineScope = CoroutineScope(syncContext)

    private var syncJob: Job? = null

    private val _state = MutableStateFlow<SyncState>(SyncState.Preparing)
    override val state: StateFlow<SyncState> = _state

    init {
        updateState()
    }

    override fun forceSync() {
        syncJob?.takeIf { it.isCompleted }?.cancel()
        _state.value = SyncState.Syncing

        syncJob = coroutineScope.launch {
            delay(500)
            _state.value = SyncState.SyncAvailable
        }
    }

    override fun cancelSync() {
        syncJob?.cancel()
        _state.value = SyncState.Canceled
    }

    private fun updateState() {
        syncJob = coroutineScope.launch {
            val isSyncEnabled = userPreferencesRepository.syncEnabled.get()

            if (!isSyncEnabled) {
                delay(200)
                _state.value = SyncState.Disabled
                return@launch
            }


        }
    }

}
