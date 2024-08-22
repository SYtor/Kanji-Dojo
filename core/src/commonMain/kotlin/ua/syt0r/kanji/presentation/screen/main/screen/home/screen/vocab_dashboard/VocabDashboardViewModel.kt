package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.LifecycleAwareViewModel
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListMode
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksMergeRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksSortRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.use_case.SortDeckDashboardItemsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.MergeVocabDecksUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.SubscribeOnDashboardVocabDecksUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case.UpdateVocabDecksOrderUseCase
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import kotlin.time.Duration.Companion.seconds

class VocabDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    subscribeOnDashboardVocabDecksUseCase: SubscribeOnDashboardVocabDecksUseCase,
    private val sortDecksUseCase: SortDeckDashboardItemsUseCase,
    private val mergeVocabDecksUseCase: MergeVocabDecksUseCase,
    private val updateDecksOrderUseCase: UpdateVocabDecksOrderUseCase,
    private val preferencesRepository: UserPreferencesRepository,
    private val analyticsManager: AnalyticsManager
) : VocabDashboardScreenContract.ViewModel, LifecycleAwareViewModel {

    private lateinit var srsPracticeType: MutableState<ScreenVocabPracticeType>

    private val sortRequestsChannel = Channel<DecksSortRequestData>()

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val screenState: StateFlow<ScreenState> = _screenState

    override val lifecycleState: MutableStateFlow<LifecycleState> =
        MutableStateFlow(LifecycleState.Hidden)

    init {

        subscribeOnDashboardVocabDecksUseCase(lifecycleState)
            .onEach { data ->
                _screenState.value = when (data) {
                    is RefreshableData.Loading ->
                        ScreenState.Loading

                    is RefreshableData.Loaded -> {
                        if (::srsPracticeType.isInitialized.not()) {
                            val practiceType = ScreenVocabPracticeType
                                .from(preferencesRepository.vocabDashboardVocabPracticeType.get())
                            val practiceTypeState = mutableStateOf(practiceType)
                            snapshotFlow { practiceTypeState.value }
                                .onEach {
                                    preferencesRepository.vocabDashboardVocabPracticeType
                                        .set(it.preferencesType)
                                }
                                .launchIn(viewModelScope)
                            srsPracticeType = practiceTypeState
                        }

                        val screenData = data.value
                        val sortByTimeEnabled = preferencesRepository.dashboardSortByTime.get()
                        val sortedItems = sortDecksUseCase(sortByTimeEnabled, screenData.decks)
                        val listState = DeckDashboardListState(
                            items = sortedItems,
                            appliedSortByReviewTime = mutableStateOf(sortByTimeEnabled),
                            mode = mutableStateOf(DeckDashboardListMode.Browsing)
                        )
                        ScreenState.Loaded(
                            listState = listState,
                            srsPracticeType = srsPracticeType
                        )
                    }
                }

            }
            .launchIn(viewModelScope)

        sortRequestsChannel.consumeAsFlow()
            // To avoid infinite loading when rapidly clicking on apply sort button
            .debounce(1.seconds)
            .onEach { updateDecksOrderUseCase.update(it) }
            .launchIn(viewModelScope)
    }

    override fun mergeDecks(data: DecksMergeRequestData) {
        _screenState.value = ScreenState.Loading
        viewModelScope.launch { mergeVocabDecksUseCase(data) }
    }

    override fun sortDecks(data: DecksSortRequestData) {
        _screenState.value = ScreenState.Loading
        viewModelScope.launch { sortRequestsChannel.send(data) }
    }

}