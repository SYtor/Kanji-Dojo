package ua.syt0r.kanji.presentation.screen.main.screen.daily_limit

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.srs.DailyLimitConfiguration
import ua.syt0r.kanji.core.srs.DailyLimitManager
import ua.syt0r.kanji.presentation.screen.main.screen.daily_limit.DailyLimitScreenContract.ScreenState

class DailyLimitScreenViewModel(
    private val viewModelScope: CoroutineScope,
    private val dailyLimitManager: DailyLimitManager,
    private val analyticsManager: AnalyticsManager
) : DailyLimitScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    init {
        viewModelScope.launch {
            val configuration = dailyLimitManager.getConfiguration()
            _state.value = ScreenState.Loaded(
                enabled = mutableStateOf(configuration.enabled),
                newLimit = mutableStateOf(configuration.newLimit),
                dueLimit = mutableStateOf(configuration.dueLimit)
            )
        }
    }

    override fun saveChanges() {
        val loadedState = _state.value as? ScreenState.Loaded ?: return
        _state.value = ScreenState.Saving
        viewModelScope.launch {
            dailyLimitManager.updateConfiguration(
                configuration = loadedState.run {
                    DailyLimitConfiguration(
                        enabled = enabled.value,
                        newLimit = newLimit.value,
                        dueLimit = dueLimit.value
                    )
                }
            )
            _state.value = ScreenState.Done
        }
    }

    private fun String.asValidLimitNumber(): Int? = toIntOrNull()?.takeIf { it >= 0 }

}