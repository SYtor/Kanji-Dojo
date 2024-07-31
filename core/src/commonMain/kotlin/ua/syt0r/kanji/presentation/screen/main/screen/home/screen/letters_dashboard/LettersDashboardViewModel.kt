package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import ua.syt0r.kanji.core.srs.use_case.NotifySrsPreferencesChangedUseCase
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.LifecycleAwareViewModel
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListMode
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksMergeRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksSortRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.use_case.SortDeckDashboardItemsUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract.ScreenState
import kotlin.time.Duration.Companion.seconds


@OptIn(FlowPreview::class)
class LettersDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    loadDataUseCase: LettersDashboardScreenContract.LoadDataUseCase,
    private val sortDecksUseCase: SortDeckDashboardItemsUseCase,
    private val updateSortUseCase: LettersDashboardScreenContract.UpdateSortUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notifySrsPreferencesChangedUseCase: NotifySrsPreferencesChangedUseCase,
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
                        val sortByTimeEnabled = userPreferencesRepository.dashboardSortByTime.get()
                        val sortedItems = sortDecksUseCase(sortByTimeEnabled, screenData.items)
                        val listState = DeckDashboardListState(
                            items = sortedItems,
                            appliedSortByReviewTime = mutableStateOf(sortByTimeEnabled),
                            mode = mutableStateOf(DeckDashboardListMode.Browsing)
                        )
                        ScreenState.Loaded(
                            listState = listState,
                            dailyIndicatorData = screenData.dailyIndicatorData
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

    override fun updateDailyGoal(configuration: DailyGoalConfiguration) {
        viewModelScope.launch {
            userPreferencesRepository.dailyLimitEnabled.set(configuration.enabled)
            userPreferencesRepository.dailyLearnLimit.set(configuration.learnLimit)
            userPreferencesRepository.dailyReviewLimit.set(configuration.reviewLimit)
            notifySrsPreferencesChangedUseCase()
            analyticsManager.sendEvent("daily_goal_update") {
                put("enabled", configuration.enabled)
                put("learn_limit", configuration.learnLimit)
                put("review_limit", configuration.reviewLimit)
            }
        }
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

    override fun reportScreenShown() {
        analyticsManager.setScreen("practice_dashboard")
    }

}