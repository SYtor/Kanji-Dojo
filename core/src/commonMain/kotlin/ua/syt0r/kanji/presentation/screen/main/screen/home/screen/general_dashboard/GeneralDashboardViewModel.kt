package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.BuildKonfig
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.LifecycleAwareViewModel
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.use_case.SubscribeOnGeneralDashboardScreenDataUseCase

class GeneralDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    private val subscribeOnScreenDataUseCase: SubscribeOnGeneralDashboardScreenDataUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
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
            .onEach {
                userPreferencesRepository.lastAppVersionWhenChangesDialogShown
                    .set(BuildKonfig.versionName)
            }
            .launchIn(viewModelScope)

        snapshotFlow { showTutorialHint.value }
            .onEach { userPreferencesRepository.tutorialSeen.set(true) }
            .launchIn(viewModelScope)

        if (letterDecksData is LetterDecksData.Data)
            snapshotFlow { letterDecksData.practiceType.value }
                .onEach { userPreferencesRepository.generalDashboardLetterPracticeType.set(it.preferencesType) }
                .launchIn(viewModelScope)

        if (vocabDecksInfo is VocabDecksData.Data)
            snapshotFlow { vocabDecksInfo.practiceType.value }
                .onEach { userPreferencesRepository.generalDashboardVocabPracticeType.set(it.preferencesType) }
                .launchIn(viewModelScope)

    }

}