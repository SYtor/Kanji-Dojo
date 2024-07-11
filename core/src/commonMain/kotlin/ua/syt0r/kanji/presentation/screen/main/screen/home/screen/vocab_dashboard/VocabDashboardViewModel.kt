package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
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
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.BottomSheetState
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

    private lateinit var srsPracticeType: VocabPracticeType

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    private val _bottomSheetState = MutableStateFlow<BottomSheetState>(BottomSheetState.Loading)

    override val screenState: StateFlow<ScreenState> = _screenState
    override val bottomSheetState: StateFlow<BottomSheetState> = _bottomSheetState

    override val lifecycleState: MutableStateFlow<LifecycleState> =
        MutableStateFlow(LifecycleState.Hidden)

    init {
        var lastSelectedDeck: DashboardVocabDeck? = null

        subscribeOnDashboardVocabDecksUseCase(lifecycleState)
            .onEach { data ->
                _screenState.value = when (data) {
                    is RefreshableData.Loading -> {
                        lastSelectedDeck = _bottomSheetState.value
                            .let { it as? BottomSheetState.DeckSelected }
                            ?.deck
                        _bottomSheetState.value = BottomSheetState.Loading
                        ScreenState.Loading
                    }

                    is RefreshableData.Loaded -> {
                        val decks = data.value

                        srsPracticeType = VocabPracticeType.from(
                            preferencesRepository.vocabPracticeType.get()
                        )

                        updateBottomSheetState(decks, lastSelectedDeck)

                        ScreenState.Loaded(
                            userDecks = decks.userDecks,
                            defaultDecks = decks.defaultDecks
                        )
                    }
                }

            }
            .launchIn(viewModelScope)
    }

    override fun select(deck: DashboardVocabDeck) {
        val srsPracticeTypeState = mutableStateOf(srsPracticeType)
        val wordsState = MutableStateFlow<VocabPracticePreviewState>(
            value = VocabPracticePreviewState.Loading
        )

        _bottomSheetState.value = BottomSheetState.DeckSelected(
            deck = deck,
            srsPracticeType = srsPracticeTypeState,
            words = wordsState
        )

        viewModelScope.launch {
            snapshotFlow { srsPracticeTypeState.value }
                .onEach {
                    srsPracticeType = it
                    preferencesRepository.vocabPracticeType.set(it.preferencesType)
                }
                .launchIn(viewModelScope)

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
                else _bottomSheetState.value = BottomSheetState.Hidden
            }
        }
    }

}