package ua.syt0r.kanji.presentation.screen.main.screen.sync

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.StateFlow

interface SyncScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Guide(
            val isSignedIn: Boolean
        ) : ScreenState

        data class SyncEnabled(
            val autoSync: MutableState<Boolean>,
            val lastSyncData: String
        ) : ScreenState

        object AccountError : ScreenState

    }

}