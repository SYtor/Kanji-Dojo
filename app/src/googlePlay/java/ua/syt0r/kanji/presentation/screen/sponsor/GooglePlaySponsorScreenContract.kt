package ua.syt0r.kanji.presentation.screen.sponsor

import android.app.Activity
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.StateFlow

interface GooglePlaySponsorScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun loadInputState()
        fun startPurchase(activity: Activity)
        fun retry(activity: Activity)
        fun reportScreenShown()
    }

    sealed interface ScreenState {
        data object Init : ScreenState
        data object Loading : ScreenState

        data class Error(
            val message: String?
        ) : ScreenState

        data class Input(
            val email: MutableState<String>,
            val message: MutableState<String>,
            val buttonEnabled: MutableState<Boolean>,
            val formattedPrice: String
        ) : ScreenState

        data object Completed : ScreenState
    }

}