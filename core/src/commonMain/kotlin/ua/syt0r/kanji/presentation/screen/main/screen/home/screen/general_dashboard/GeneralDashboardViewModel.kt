package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.BuildConfig
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.LifecycleAwareViewModel
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.use_case.SubscribeOnGeneralDashboardScreenDataUseCase

class GeneralDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    private val subscribeOnScreenDataUseCase: SubscribeOnGeneralDashboardScreenDataUseCase,
    private val appPreferences: PreferencesContract.AppPreferences,
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

    }

    private fun ScreenState.Loaded.handleUpdates() {
        snapshotFlow { showAppVersionChangeHint.value }
            .drop(1)
            .onEach {
                appPreferences.lastAppVersionWhenChangesDialogShown
                    .set(BuildConfig.versionName)
            }
            .launchIn(viewModelScope)

        snapshotFlow { showTutorialHint.value }
            .drop(1)
            .onEach { appPreferences.tutorialSeen.set(true) }
            .launchIn(viewModelScope)

        if (letterDecksData is LetterDecksData.Data)
            snapshotFlow { letterDecksData.practiceType.value }
                .drop(1)
                .onEach { appPreferences.generalDashboardLetterPracticeType.set(it.preferencesType) }
                .launchIn(viewModelScope)

        if (vocabDecksInfo is VocabDecksData.Data)
            snapshotFlow { vocabDecksInfo.practiceType.value }
                .drop(1)
                .onEach { appPreferences.generalDashboardVocabPracticeType.set(it.preferencesType) }
                .launchIn(viewModelScope)

    }

}