package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard

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
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.app_state.AppStateManager
import ua.syt0r.kanji.core.app_state.DailyGoalConfiguration
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardScreenContract.ScreenState
import kotlin.time.Duration.Companion.seconds


@OptIn(FlowPreview::class)
class PracticeDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    loadDataUseCase: PracticeDashboardScreenContract.LoadDataUseCase,
    private val applySortUseCase: PracticeDashboardScreenContract.ApplySortUseCase,
    private val updateSortUseCase: PracticeDashboardScreenContract.UpdateSortUseCase,
    private val appStateManager: AppStateManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mergePracticeSetsUseCase: PracticeDashboardScreenContract.MergePracticeSetsUseCase,
    private val analyticsManager: AnalyticsManager
) : PracticeDashboardScreenContract.ViewModel {

    override val state = mutableStateOf<ScreenState>(ScreenState.Loading)

    private var sortByTimeEnabled: Boolean = false
    private lateinit var listMode: MutableStateFlow<PracticeDashboardListMode>

    private val sortRequestsChannel = Channel<PracticeReorderRequestData>()

    init {
        loadDataUseCase.load()
            .onEach {
                Logger.d("applying new state")
                sortByTimeEnabled = userPreferencesRepository.dashboardSortByTime.get()
                val sortedItems = applySortUseCase.sort(sortByTimeEnabled, it.items)
                listMode = MutableStateFlow(PracticeDashboardListMode.Default(sortedItems))
                state.value = ScreenState.Loaded(
                    mode = listMode,
                    dailyIndicatorData = it.dailyIndicatorData
                )
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
            appStateManager.invalidate()
            analyticsManager.sendEvent("daily_goal_update") {
                put("enabled", configuration.enabled)
                put("learn_limit", configuration.learnLimit)
                put("review_limit", configuration.reviewLimit)
            }
        }
    }

    override fun enablePracticeMergeMode() {
        listMode.value = PracticeDashboardListMode.MergeMode(
            items = listMode.value.items,
            selected = mutableStateOf(emptySet()),
            title = mutableStateOf("")
        )
    }

    override fun merge(data: PracticeMergeRequestData) {
        Logger.d("data[$data]")
        state.value = ScreenState.Loading
        viewModelScope.launch { mergePracticeSetsUseCase.merge(data) }
    }

    override fun enablePracticeReorderMode() {
        val items = listMode.value.items
        listMode.value = PracticeDashboardListMode.SortMode(
            items = items,
            reorderedList = mutableStateOf(items),
            sortByReviewTime = mutableStateOf(sortByTimeEnabled)
        )
    }

    override fun reorder(data: PracticeReorderRequestData) {
        Logger.d("data[$data]")
        state.value = ScreenState.Loading
        sortByTimeEnabled = data.sortByTime
        viewModelScope.launch { sortRequestsChannel.send(data) }
    }

    override fun enableDefaultMode() {
        listMode.value = PracticeDashboardListMode.Default(
            items = listMode.value.items
        )
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("practice_dashboard")
    }

}