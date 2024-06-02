package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.app_state.AppStateManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.CharacterReviewResult
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreenContract.ScreenState
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class StatsViewModel(
    viewModelScope: CoroutineScope,
    appStateManager: AppStateManager,
    private val practiceRepository: LetterPracticeRepository,
    private val timeUtils: TimeUtils,
    private val analyticsManager: AnalyticsManager
) : StatsScreenContract.ViewModel {

    companion object {
        private val SingleDurationLimit = 1.minutes
    }

    private val screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)

    override val state: StateFlow<ScreenState> = screenState

    init {
        appStateManager.appStateFlow
            .map {
                Logger.d(it.toString())
                if (it.isLoading) return@map ScreenState.Loading

                val today = timeUtils.getCurrentDate()

                val timeZone = TimeZone.currentSystemDefault()
                val periodStart = LocalDate(today.year, 1, 1).atStartOfDayIn(timeZone)
                val periodEnd = LocalDate(today.year + 1, 1, 1).atStartOfDayIn(timeZone)

                val reviews = practiceRepository.getReviews(periodStart, periodEnd)
                    .mapValues { (_, instant) -> instant.toLocalDateTime(timeZone).date }
                    .toList()

                val dateToReviews: Map<LocalDate, List<CharacterReviewResult>> = reviews
                    .groupBy { it.second }
                    .toList()
                    .associate { it.first to it.second.map { it.first } }

                val todayReviews = dateToReviews[today] ?: emptyList()

                ScreenState.Loaded(
                    today = today,
                    yearlyPractices = dateToReviews.mapValues { (_, practices) -> practices.size },
                    todayReviews = todayReviews.size,
                    todayTimeSpent = todayReviews.map { it.reviewDuration }
                        .fold(Duration.ZERO) { acc, duration ->
                            acc.plus(
                                min(
                                    a = duration.inWholeMilliseconds,
                                    b = SingleDurationLimit.inWholeMilliseconds
                                ).milliseconds
                            )
                        },
                    totalReviews = practiceRepository
                        .getTotalReviewsCount()
                        .toInt(),
                    totalTimeSpent = practiceRepository
                        .getTotalPracticeTime(SingleDurationLimit.inWholeMilliseconds)
                        .milliseconds,
                    totalCharactersStudied = practiceRepository
                        .getTotalUniqueReviewedCharactersCount()
                        .toInt()
                )
            }
            .onEach { screenState.value = it }
            .launchIn(viewModelScope)
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("stats")
    }

}