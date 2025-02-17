package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.runtime.MutableState
import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.core.user_data.database.SavedVocabCard
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType


data class DecksStudyProgress<ItemType>(
    val newToDeckIdMap: Map<ItemType, Long>,
    val dueToDeckIdMap: Map<ItemType, Long>,
) {
    val combined: Map<ItemType, Long> = newToDeckIdMap + dueToDeckIdMap
}

typealias LetterDecksStudyProgress = DecksStudyProgress<String>
typealias VocabDecksStudyProgress = DecksStudyProgress<Long>

sealed interface LetterDecksData {

    object NoDecks : LetterDecksData

    data class Data(
        val practiceType: MutableState<ScreenLetterPracticeType>,
        val studyProgressMap: Map<ScreenLetterPracticeType, LetterDecksStudyProgress>
    ) : LetterDecksData {
        val pendingReviewsMap = studyProgressMap.mapValues { it.value.combined.isNotEmpty() }
    }

}

sealed interface VocabDecksData {

    object NoDecks : VocabDecksData

    data class Data(
        val practiceType: MutableState<ScreenVocabPracticeType>,
        val studyProgressMap: Map<ScreenVocabPracticeType, VocabDecksStudyProgress>
    ) : VocabDecksData {
        val pendingReviewsMap = studyProgressMap.mapValues { it.value.combined.isNotEmpty() }
    }

}

data class StreakCalendarItem(
    val date: LocalDate,
    val anyReviews: Boolean
)