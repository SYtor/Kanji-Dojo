package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.StateFlow

interface GeneralDashboardScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
    }

    sealed interface ScreenState {

        data object Loading : ScreenState

        data class Loaded(
            val showAppVersionChangeHint: MutableState<Boolean>,
            val showTutorialHint: MutableState<Boolean>,
            val letterDecksData: LetterDecksData,
            val vocabDecksInfo: VocabDecksData,
            val streakData: StreakData
        ) : ScreenState

    }

    data class StreakData(
        val calendarItems: List<StreakCalendarItem>,
        val currentStreak: Int,
        val longestStreak: Int
    )

}
