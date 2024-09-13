package ua.syt0r.kanji.presentation.screen.main.screen.daily_limit

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType

interface DailyLimitScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun saveChanges()
    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Loaded(
            val enabled: MutableState<Boolean>,
            val isLetterLimitCombined: MutableState<Boolean>,
            val letterCombined: LimitItem,
            val letterSeparate: Map<ScreenLetterPracticeType, LimitItem>,
            val isVocabLimitCombined: MutableState<Boolean>,
            val vocabCombined: LimitItem,
            val vocabSeparate: Map<ScreenVocabPracticeType, LimitItem>,
        ) : ScreenState

        object Saving : ScreenState

        object Done : ScreenState

    }

}
