package ua.syt0r.kanji.presentation.screen.main.screen.about

import ua.syt0r.kanji.core.analytics.AnalyticsManager

class AboutScreenViewModel(
    private val analyticsManager: AnalyticsManager
) : AboutScreenContract.ViewModel {

    override fun reportUrlClick(url: String) {
        analyticsManager.sendEvent("about_url_click") { put("url", url) }
    }
}