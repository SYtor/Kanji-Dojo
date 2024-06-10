package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard

import androidx.compose.runtime.MutableState
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import kotlin.time.Duration

data class PracticeDashboardItem(
    val practiceId: Long,
    val title: String,
    val position: Int,
    val timeSinceLastPractice: Duration?,
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

data class PracticeDashboardScreenData(
    val items: List<PracticeDashboardItem>,
    val dailyIndicatorData: DailyIndicatorData
)

sealed interface PracticeDashboardListMode {

    val items: List<PracticeDashboardItem>

    data class Default(
        override val items: List<PracticeDashboardItem>
    ) : PracticeDashboardListMode

    data class MergeMode(
        override val items: List<PracticeDashboardItem>,
        val selected: MutableState<Set<Long>>,
        val title: MutableState<String>
    ) : PracticeDashboardListMode

    data class SortMode(
        override val items: List<PracticeDashboardItem>,
        val reorderedList: MutableState<List<PracticeDashboardItem>>,
        val sortByReviewTime: MutableState<Boolean>
    ) : PracticeDashboardListMode

}

data class PracticeMergeRequestData(
    val title: String,
    val practiceIdList: List<Long>
)

data class PracticeReorderRequestData(
    val reorderedList: List<PracticeDashboardItem>,
    val sortByTime: Boolean
)
