package ua.syt0r.kanji.presentation.screen.main.screen.account

import kotlinx.coroutines.flow.StateFlow

interface JvmAccountScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun signIn()
        fun signOut()
    }

    sealed interface ScreenState {
        object SignedOut : ScreenState
        object StartingSever : ScreenState
        data class WaitingForSignIn(val serverPort: Int) : ScreenState
        object LoadingUserData : ScreenState
        object Loaded : ScreenState
    }

}