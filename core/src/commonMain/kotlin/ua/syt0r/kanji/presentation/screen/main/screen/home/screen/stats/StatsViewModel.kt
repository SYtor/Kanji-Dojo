package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.LifecycleAwareViewModel
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.use_case.SubscribeOnStatsDataUseCase

class StatsViewModel(
    viewModelScope: CoroutineScope,
    subscribeOnStatsDataUseCase: SubscribeOnStatsDataUseCase,
    private val analyticsManager: AnalyticsManager,
) : StatsScreenContract.ViewModel, LifecycleAwareViewModel {

    override val lifecycleState: MutableStateFlow<LifecycleState> =
        MutableStateFlow(LifecycleState.Hidden)

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    init {
        subscribeOnStatsDataUseCase(lifecycleState)
            .map {
                when (it) {
                    is RefreshableData.Loading -> ScreenState.Loading
                    is RefreshableData.Loaded -> ScreenState.Loaded(it.value)
                }
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

}