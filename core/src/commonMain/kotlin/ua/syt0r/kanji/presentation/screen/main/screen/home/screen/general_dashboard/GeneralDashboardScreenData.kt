package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

data class DashboardSrsModeData<T>(
    val modeTitle: String,
    val total: List<T>,
    val done: List<T>,
    val due: List<T>,
    val new: List<T>
)