package ua.syt0r.kanji.presentation.screen.main.screen.sync

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.StateFlow

interface SyncScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Loaded(
            val isSignedIn: Boolean,
            val isSubscriptionActive: Boolean,
            val autoSync: MutableState<Boolean>
        ) : ScreenState

    }

}