package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.user_data.preferences.PracticeUserPreferencesRepository
import ua.syt0r.kanji.presentation.LifecycleAwareViewModel
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.GetVocabDeckWordsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.SubscribeOnDashboardVocabDecksUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.VocabDecks
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType

class VocabDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    subscribeOnDashboardVocabDecksUseCase: SubscribeOnDashboardVocabDecksUseCase,
    private val getVocabDeckWordsUseCase: GetVocabDeckWordsUseCase,
    private val preferencesRepository: PracticeUserPreferencesRepository,
    private val analyticsManager: AnalyticsManager
) : VocabDashboardScreenContract.ViewModel, LifecycleAwareViewModel {

    private val invalidationRequests = Channel<Unit>()
    private val displayPracticeType = mutableStateOf(VocabPracticeType.Flashcard)
    private val deckSelectionState = mutableStateOf<VocabDeckSelectionState>(
        VocabDeckSelectionState.Loading
    )

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    override val lifecycleState: MutableStateFlow<LifecycleState> =
        MutableStateFlow(LifecycleState.Hidden)

    init {
        snapshotFlow { displayPracticeType.value }
            .onEach { preferencesRepository.vocabPracticeType.set(it.preferencesType) }
            .launchIn(viewModelScope)

        var lastSelectedDeck: DashboardVocabDeck? = null

        subscribeOnDashboardVocabDecksUseCase(lifecycleState)
            .onEach { data ->
                _state.value = when (data) {
                    is RefreshableData.Loading -> {
                        lastSelectedDeck = deckSelectionState.value
                            .let { it as? VocabDeckSelectionState.DeckSelected }
                            ?.deck
                        deckSelectionState.value = VocabDeckSelectionState.Loading
                        ScreenState.Loading
                    }

                    is RefreshableData.Loaded -> {
                        val decks = data.value
                        displayPracticeType.value = VocabPracticeType.from(
                            preferencesRepository.vocabPracticeType.get()
                        )

                        updateBottomSheetState(decks, lastSelectedDeck)

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

    override fun select(deck: DashboardVocabDeck) {
        val wordsState = MutableStateFlow<VocabPracticePreviewState>(
            value = VocabPracticePreviewState.Loading
        )

        deckSelectionState.value = VocabDeckSelectionState.DeckSelected(
            deck = deck,
            displayPracticeType = displayPracticeType,
            words = wordsState
        )

        viewModelScope.launch {
            wordsState.value = VocabPracticePreviewState.Loaded(
                words = getVocabDeckWordsUseCase(deck.words)
            )
        }
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("vocab_dashboard")
    }

    private fun updateBottomSheetState(decks: VocabDecks, lastSelectedDeck: DashboardVocabDeck?) {
        when (lastSelectedDeck) {
            null -> {}
            is DashboardVocabDeck.Default -> {
                val updatedDeck = decks.defaultDecks.first { it.index == lastSelectedDeck.index }
                select(updatedDeck)
            }

            is DashboardVocabDeck.User -> {
                val updatedDeck = decks.userDecks.find { it.id == lastSelectedDeck.id }
                if (updatedDeck != null) select(updatedDeck)
                else deckSelectionState.value = VocabDeckSelectionState.Hidden
            }
        }
    }

}