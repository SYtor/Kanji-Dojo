package ua.syt0r.kanji.presentation.screen.main.screen.daily_limit

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow

interface DailyLimitScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun saveChanges()
    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Loaded(
            val enabled: MutableState<Boolean>,
            val newLimitInput: MutableState<String>,
            val newLimitValidated: State<Int?>,
            val dueLimitInput: MutableState<String>,
            val dueLimitValidated: State<Int?>,
        ) : ScreenState

        object Saving : ScreenState

        object Done : ScreenState

    }

}
