package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.use_case.StatsData

interface StatsScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun reportScreenShown()
    }

    sealed interface ScreenState {
        object Loading : ScreenState
        data class Loaded(
            val stats: StatsData
        ) : ScreenState
    }

}
