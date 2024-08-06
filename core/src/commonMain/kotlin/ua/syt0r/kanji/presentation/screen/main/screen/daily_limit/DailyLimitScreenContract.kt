package ua.syt0r.kanji.presentation.screen.main.screen.daily_limit

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.srs.DailyLimitConfiguration

interface DailyLimitScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun saveChanges()
        fun reportScreenShown()
    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Loaded(
            val configuration: DailyLimitConfiguration,
        ) : ScreenState

        object Saving : ScreenState

        object Done : ScreenState

    }

}
