package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import androidx.compose.runtime.MutableState
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import kotlin.time.Duration

data class LettersDashboardItem(
    val deckId: Long,
    val title: String,
    val position: Int,
    val timeSinceLastReview: Duration?,
    val writingProgress: PracticeStudyProgress,
    val readingProgress: PracticeStudyProgress
)

data class PracticeStudyProgress(
    val all: List<String>,
    val known: List<String>,
    val review: List<String>,
    val new: List<String>,
    val quickLearn: List<String>,
    val quickReview: List<String>,
) {

    val completionPercentage = when {
        all.isEmpty() -> 100f
        else -> (known.size + review.size).toFloat() / all.size * 100
    }

}

data class DailyIndicatorData(
    val configuration: DailyGoalConfiguration,
    val progress: DailyProgress
)

sealed interface DailyProgress {
    object Completed : DailyProgress
    data class StudyOnly(val count: Int) : DailyProgress
    data class ReviewOnly(val count: Int) : DailyProgress
    data class StudyAndReview(val study: Int, val review: Int) : DailyProgress
    object Disabled : DailyProgress
}

data class LettersDashboardScreenData(
    val items: List<LettersDashboardItem>,
    val dailyIndicatorData: DailyIndicatorData
)

sealed interface LettersDashboardListMode {

    val items: List<LettersDashboardItem>

    data class Default(
        override val items: List<LettersDashboardItem>
    ) : LettersDashboardListMode

    data class MergeMode(
        override val items: List<LettersDashboardItem>,
        val selected: MutableState<Set<Long>>,
        val title: MutableState<String>
    ) : LettersDashboardListMode

    data class SortMode(
        override val items: List<LettersDashboardItem>,
        val reorderedList: MutableState<List<LettersDashboardItem>>,
        val sortByReviewTime: MutableState<Boolean>
    ) : LettersDashboardListMode

}

data class LetterDecksMergeRequestData(
    val title: String,
    val deckIds: List<Long>
)

data class LetterDecksReorderRequestData(
    val reorderedList: List<LettersDashboardItem>,
    val sortByTime: Boolean
)
