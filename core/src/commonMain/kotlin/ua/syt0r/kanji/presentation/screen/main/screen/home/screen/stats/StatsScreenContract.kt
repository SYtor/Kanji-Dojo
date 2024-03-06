package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

interface StatsScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun reportScreenShown()
    }

    sealed interface ScreenState {
        object Loading : ScreenState
        data class Loaded(
            val today: LocalDate,
            val yearlyPractices: Map<LocalDate, Int>,
            val todayReviews: Int,
            val todayTimeSpent: Duration,
            val totalReviews: Int,
            val totalTimeSpent: Duration,
            val totalCharactersStudied: Int
        ) : ScreenState
    }

}
