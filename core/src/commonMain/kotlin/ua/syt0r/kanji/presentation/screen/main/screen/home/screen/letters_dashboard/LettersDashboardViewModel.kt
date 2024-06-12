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
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract.ScreenState
import kotlin.time.Duration.Companion.seconds


@OptIn(FlowPreview::class)
class LettersDashboardViewModel(
    private val viewModelScope: CoroutineScope,
    loadDataUseCase: LettersDashboardScreenContract.LoadDataUseCase,
    private val applySortUseCase: LettersDashboardScreenContract.ApplySortUseCase,
    private val updateSortUseCase: LettersDashboardScreenContract.UpdateSortUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notifySrsPreferencesChangedUseCase: NotifySrsPreferencesChangedUseCase,
    private val mergeDecksUseCase: LettersDashboardScreenContract.MergeDecksUseCase,
    private val analyticsManager: AnalyticsManager
) : LettersDashboardScreenContract.ViewModel, LifecycleAwareViewModel {

    override val lifecycleState: MutableStateFlow<LifecycleState> =
        MutableStateFlow(LifecycleState.Hidden)

    private var sortByTimeEnabled: Boolean = false
    private lateinit var listMode: MutableStateFlow<LettersDashboardListMode>

    private val sortRequestsChannel = Channel<LetterDecksReorderRequestData>()

    override val state = mutableStateOf<ScreenState>(ScreenState.Loading)

    init {
        loadDataUseCase.load(lifecycleState)
            .onEach {
                state.value = when (it) {
                    is RefreshableData.Loaded -> {
                        val screenData = it.value
                        sortByTimeEnabled = userPreferencesRepository.dashboardSortByTime.get()
                        val sortedItems = applySortUseCase.sort(sortByTimeEnabled, screenData.items)
                        listMode = MutableStateFlow(LettersDashboardListMode.Default(sortedItems))
                        ScreenState.Loaded(
                            mode = listMode,
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

    override fun enablePracticeMergeMode() {
        listMode.value = LettersDashboardListMode.MergeMode(
            items = listMode.value.items,
            selected = mutableStateOf(emptySet()),
            title = mutableStateOf("")
        )
    }

    override fun merge(data: LetterDecksMergeRequestData) {
        Logger.d("data[$data]")
        state.value = ScreenState.Loading
        viewModelScope.launch { mergeDecksUseCase.merge(data) }
    }

    override fun enablePracticeReorderMode() {
        val items = listMode.value.items
        listMode.value = LettersDashboardListMode.SortMode(
            items = items,
            reorderedList = mutableStateOf(items),
            sortByReviewTime = mutableStateOf(sortByTimeEnabled)
        )
    }

    override fun reorder(data: LetterDecksReorderRequestData) {
        Logger.d("data[$data]")
        state.value = ScreenState.Loading
        sortByTimeEnabled = data.sortByTime
        viewModelScope.launch { sortRequestsChannel.send(data) }
    }

    override fun enableDefaultMode() {
        listMode.value = LettersDashboardListMode.Default(
            items = listMode.value.items
        )
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("practice_dashboard")
    }

}