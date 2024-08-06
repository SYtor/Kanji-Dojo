package ua.syt0r.kanji.presentation.screen.main.screen.daily_limit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.screen.main.screen.daily_limit.DailyLimitScreenContract.ScreenState

class DailyLimitScreenViewModel(
    viewModelScope: CoroutineScope,
    private val analyticsManager: AnalyticsManager
) : DailyLimitScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    override fun saveChanges() {
        TODO("Not yet implemented")
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("daily_limit")
    }

}