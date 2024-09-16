package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckDashboardItem


data class LettersDashboardScreenData(
    val items: List<LetterDeckDashboardItem>,
    val dailyLimitEnabled: Boolean
)
