package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common

import androidx.compose.runtime.MutableState
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenPracticeType
import kotlin.time.Duration


data class DeckStudyProgress<T>(
    val all: List<T>,
    val known: List<T>,
    val review: List<T>,
    val new: List<T>,
    val dailyNew: List<T>,
    val dailyDue: List<T>,
) {

    fun completionPercentage(): Float = when {
        all.isEmpty() -> 100f
        else -> (known.size + review.size).toFloat() / all.size * 100
    }

}

typealias LetterDeckStudyProgress = DeckStudyProgress<String>
typealias VocabDeckStudyProgress = DeckStudyProgress<Long>

interface DeckDashboardItem {
    val deckId: Long
    val title: String
    val position: Int
    val elapsedSinceLastReview: Duration?
    val studyProgress: Map<ScreenPracticeType, DeckStudyProgress<out Any>>
}

data class LetterDeckDashboardItem(
    override val deckId: Long,
    override val title: String,
    override val position: Int,
    override val elapsedSinceLastReview: Duration?,
    val writingProgress: LetterDeckStudyProgress,
    val readingProgress: LetterDeckStudyProgress
) : DeckDashboardItem {

    override val studyProgress: Map<ScreenPracticeType, LetterDeckStudyProgress> = mapOf(
        ScreenLetterPracticeType.Writing to writingProgress,
        ScreenLetterPracticeType.Reading to readingProgress
    )

}

data class VocabDeckDashboardItem(
    override val deckId: Long,
    override val title: String,
    override val position: Int,
    override val elapsedSinceLastReview: Duration?,
    override val studyProgress: Map<ScreenPracticeType, VocabDeckStudyProgress>,
) : DeckDashboardItem

data class DeckDashboardListState(
    val items: List<DeckDashboardItem>,
    val appliedSortByReviewTime: MutableState<Boolean>,
    val mode: MutableState<DeckDashboardListMode>
)

sealed interface DeckDashboardListMode {

    object Browsing : DeckDashboardListMode

    data class MergeMode(
        val selected: MutableState<Set<Long>>,
        val title: MutableState<String>
    ) : DeckDashboardListMode

    data class SortMode(
        val reorderedList: MutableState<List<DeckDashboardItem>>,
        val sortByReviewTime: MutableState<Boolean>
    ) : DeckDashboardListMode

}

data class DecksMergeRequestData(
    val title: String,
    val deckIds: List<Long>
)

data class DecksSortRequestData(
    val reorderedList: List<DeckDashboardItem>,
    val sortByTime: Boolean
)