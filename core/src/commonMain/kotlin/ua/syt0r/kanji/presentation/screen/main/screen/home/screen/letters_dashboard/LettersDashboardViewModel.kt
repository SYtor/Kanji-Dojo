package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.LifecycleAwareViewModel
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListMode
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksMergeRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksSortRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckDashboardPracticeTypeItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract.ScreenState
import kotlin.time.Duration.Companion.seconds


@OptIn(FlowPreview::class)
class LettersDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    loadDataUseCase: LettersDashboardScreenContract.LoadDataUseCase,
    private val updateSortUseCase: LettersDashboardScreenContract.UpdateSortUseCase,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val mergeDecksUseCase: LettersDashboardScreenContract.MergeDecksUseCase,
    private val analyticsManager: AnalyticsManager
) : LettersDashboardScreenContract.ViewModel, LifecycleAwareViewModel {

    override val lifecycleState: MutableStateFlow<LifecycleState> =
        MutableStateFlow(LifecycleState.Hidden)

    private val sortRequestsChannel = Channel<DecksSortRequestData>()

    override val state = mutableStateOf<ScreenState>(ScreenState.Loading)

    init {
        loadDataUseCase.load(lifecycleState)
            .onEach {
                state.value = when (it) {
                    is RefreshableData.Loaded -> {
                        val screenData = it.value
                        val sortByTimeEnabled = appPreferences
                            .letterDashboardSortByTime.get()
                        val listState = DeckDashboardListState(
                            items = screenData.items,
                            sortByReviewTime = sortByTimeEnabled,
                            showDailyNewIndicator = screenData.dailyLimitEnabled,
                            mode = mutableStateOf(DeckDashboardListMode.Browsing)
                        )

                        val practiceTypeItems = ScreenLetterPracticeType.values()
                            .map { practiceType ->
                                val hasPendingReviews = listState.items.any {
                                    it.studyProgress.getValue(practiceType).run {
                                        dailyNew.isNotEmpty() || dailyDue.isNotEmpty()
                                    }
                                }
                                LetterDeckDashboardPracticeTypeItem(
                                    practiceType = practiceType,
                                    hasPendingReviews = hasPendingReviews
                                )
                            }

                        val practiceType = ScreenLetterPracticeType.from(
                            appPreferences.letterDashboardPracticeType.get()
                        )

                        val selectedItemState = mutableStateOf(
                            practiceTypeItems.first { it.practiceType == practiceType }
                        )

                        snapshotFlow { selectedItemState.value }
                            .map { it.practiceType.preferencesType }
                            .onEach { appPreferences.letterDashboardPracticeType.set(it) }
                            .launchIn(viewModelScope)

                        ScreenState.Loaded(
                            listState = listState,
                            practiceTypeItems = practiceTypeItems,
                            selectedPracticeTypeItem = selectedItemState,
                        )
                    }

                    is RefreshableData.Loading -> ScreenState.Loading
                }
            }
            .launchIn(viewModelScope)

        sortRequestsChannel.consumeAsFlow()
            // To avoid infinite loading when rapidly clicking on apply sort button
            .debounce(1.seconds)
            .onEach { updateSortUseCase.update(it) }
            .launchIn(viewModelScope)
    }

    override fun mergeDecks(data: DecksMergeRequestData) {
        Logger.d("data[$data]")
        state.value = ScreenState.Loading
        viewModelScope.launch { mergeDecksUseCase(data) }
    }

    override fun sortDecks(data: DecksSortRequestData) {
        Logger.d("data[$data]")
        state.value = ScreenState.Loading
        viewModelScope.launch { sortRequestsChannel.send(data) }
    }

}