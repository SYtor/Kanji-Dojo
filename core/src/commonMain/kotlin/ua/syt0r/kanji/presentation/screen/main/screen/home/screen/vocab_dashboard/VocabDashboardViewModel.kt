package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.GetVocabDeckWordsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.GetVocabDecksUseCase

class VocabDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    private val getVocabDecksUseCase: GetVocabDecksUseCase,
    private val getVocabDeckWordsUseCase: GetVocabDeckWordsUseCase,
    private val analyticsManager: AnalyticsManager
) : VocabDashboardScreenContract.ViewModel {

    private val deckSelectionState = mutableStateOf<VocabDeckSelectionState>(
        VocabDeckSelectionState.NothingSelected
    )

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    init {
        viewModelScope.launch {
            val result = getVocabDecksUseCase()
            _state.value = ScreenState.Loaded(
                userDecks = result.userDecks,
                defaultDecks = result.defaultDecks,
                deckSelectionState = deckSelectionState
            )
        }
    }

    override fun select(deck: VocabPracticeDeck) {
        val wordsState = MutableStateFlow<VocabPracticePreviewState>(
            value = VocabPracticePreviewState.Loading
        )

        deckSelectionState.value = VocabDeckSelectionState.DeckSelected(deck, wordsState)

        viewModelScope.launch {
            wordsState.value = VocabPracticePreviewState.Loaded(
                words = getVocabDeckWordsUseCase(deck.expressionIds)
            )
        }
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("vocab_dashboard")
    }
}