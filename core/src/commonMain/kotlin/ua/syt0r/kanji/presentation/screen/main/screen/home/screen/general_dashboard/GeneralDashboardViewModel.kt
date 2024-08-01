package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.LifecycleAwareViewModel
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.use_case.SubscribeOnGeneralDashboardScreenDataUseCase

class GeneralDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    private val subscribeOnScreenDataUseCase: SubscribeOnGeneralDashboardScreenDataUseCase,
    private val analyticsManager: AnalyticsManager
) : GeneralDashboardScreenContract.ViewModel,
    LifecycleAwareViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)

    override val state: StateFlow<ScreenState> = _state
    override val lifecycleState = MutableStateFlow(LifecycleState.Hidden)

    init {

        subscribeOnScreenDataUseCase(viewModelScope, lifecycleState)
            .onEach { refreshableData ->
                when (refreshableData) {
                    is RefreshableData.Loading -> _state.value = ScreenState.Loading
                    is RefreshableData.Loaded -> _state.value = refreshableData.value
                        .also { it.handleUpdates() }
                }
            }
            .launchIn(viewModelScope)

        lifecycleState.filter { it == LifecycleState.Visible }
            .distinctUntilChanged()
            .onEach { analyticsManager.setScreen("general_dashboard") }
            .launchIn(viewModelScope)

    }

    private fun ScreenState.Loaded.handleUpdates() {
        //todo save changes to repository

        snapshotFlow { showAppVersionChangeHint.value }
            .onEach { }
            .launchIn(viewModelScope)

        snapshotFlow { showTutorialHint.value }
            .onEach { }
            .launchIn(viewModelScope)

        if (letterDecksData is LetterDecksData.Data)
            snapshotFlow { letterDecksData.studyType.value }
                .onEach { }
                .launchIn(viewModelScope)

        if (vocabDecksInfo is VocabDecksData.Data)
            snapshotFlow { vocabDecksInfo.studyType.value }
                .onEach { }
                .launchIn(viewModelScope)

    }

}