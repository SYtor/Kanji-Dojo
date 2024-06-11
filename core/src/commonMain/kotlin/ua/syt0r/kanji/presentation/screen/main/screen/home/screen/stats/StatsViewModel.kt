package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.use_case.SubscribeOnStatsDataUseCase

class StatsViewModel(
    private val viewModelScope: CoroutineScope,
    subscribeOnStatsDataUseCase: SubscribeOnStatsDataUseCase,
    private val analyticsManager: AnalyticsManager,
) : StatsScreenContract.ViewModel {

    private val screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)

    private val invalidationRequests = Channel<Unit>()
    override val state: StateFlow<ScreenState> = screenState

    init {
        subscribeOnStatsDataUseCase(invalidationRequests.consumeAsFlow())
            .map {
                when (it) {
                    is RefreshableData.Loading -> ScreenState.Loading
                    is RefreshableData.Loaded -> ScreenState.Loaded(it.value)
                }
            }
            .onEach { screenState.value = it }
            .launchIn(viewModelScope)
    }

    override fun notifyScreenShown() {
        viewModelScope.launch { invalidationRequests.send(Unit) }
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("stats")
    }

}