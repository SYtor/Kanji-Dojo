package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckDashboardItem

data class DailyIndicatorData(
    val dailyLimitEnabled: Boolean,
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
    val items: List<LetterDeckDashboardItem>,
    val dailyIndicatorData: DailyIndicatorData
)
