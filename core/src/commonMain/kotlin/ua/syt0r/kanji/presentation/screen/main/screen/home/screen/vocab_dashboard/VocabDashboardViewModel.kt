package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.GetVocabDeckWordsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.GetVocabDecksUseCase

class VocabDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    getVocabDecksUseCase: GetVocabDecksUseCase,
    private val getVocabDeckWordsUseCase: GetVocabDeckWordsUseCase,
    private val analyticsManager: AnalyticsManager
) : VocabDashboardScreenContract.ViewModel {

    private val invalidationRequests = Channel<Unit>()
    private val deckSelectionState = mutableStateOf<VocabDeckSelectionState>(
        VocabDeckSelectionState.NothingSelected
    )

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    init {
        getVocabDecksUseCase(invalidationRequests.consumeAsFlow())
            .onEach { data ->
                _state.value = when (data) {
                    is RefreshableData.Loading -> {
                        ScreenState.Loading
                    }

                    is RefreshableData.Loaded -> {
                        val decks = data.value
                        deckSelectionState.value = VocabDeckSelectionState.NothingSelected
                        ScreenState.Loaded(
                            userDecks = decks.userDecks,
                            defaultDecks = decks.defaultDecks,
                            deckSelectionState = deckSelectionState
                        )
                    }
                }

            }
            .launchIn(viewModelScope)
    }

    override fun invalidate() {
        viewModelScope.launch { invalidationRequests.send(Unit) }
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