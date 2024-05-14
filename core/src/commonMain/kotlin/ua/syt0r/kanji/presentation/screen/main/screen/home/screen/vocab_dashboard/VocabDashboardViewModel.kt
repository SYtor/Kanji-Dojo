package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.ScreenState

class VocabDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    private val appDataRepository: AppDataRepository,
    private val analyticsManager: AnalyticsManager
) : VocabDashboardScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.NothingSelected)

    override val state: StateFlow<ScreenState> = _state

    override fun select(set: VocabPracticeSet) {
        val wordsState = MutableStateFlow<VocabPracticePreviewState>(
            value = VocabPracticePreviewState.Loading
        )

        _state.value = ScreenState.SelectedSet(set, wordsState)

        viewModelScope.launch {
            val words = set.expressionIds.map { appDataRepository.getWord(it) }
            wordsState.value = VocabPracticePreviewState.Loaded(words)
        }
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("vocab_dashboard")
    }
}