package ua.syt0r.kanji.presentation.screen.main.screen.sync

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.user_data.preferences.PreferencesSyncDataInfo

interface SyncScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun sync()
    }

    sealed interface ScreenState {
        object Loading : ScreenState

        data class Guide(
            val isSignedIn: Boolean
        ) : ScreenState

        object AccountError : ScreenState

        data class SyncEnabled(
            val syncState: StateFlow<SyncState>,
            val localDataState: StateFlow<PreferencesSyncDataInfo>
        ) : ScreenState

    }

}