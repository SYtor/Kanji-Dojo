package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import kotlinx.coroutines.flow.StateFlow

interface StatsScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
    }

    sealed interface ScreenState {
        data object Loading : ScreenState
        data class Loaded(
            val stats: StatsData
        ) : ScreenState
    }

}
