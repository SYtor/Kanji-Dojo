package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.use_case

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.srs.VocabPracticeType
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryRepository
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.DayStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.RefreshableStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.TimePeriodStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.TotalStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.YearMonth
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface SubscribeOnStatsDataUseCase {
    operator fun invoke(lifecycleState: StateFlow<LifecycleState>): Flow<RefreshableData<StatsData>>
}

class DefaultSubscribeOnStatsDataUseCase(
    private val letterSrsManager: LetterSrsManager,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val timeUtils: TimeUtils
) : SubscribeOnStatsDataUseCase {

    override fun invoke(
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<StatsData>> {
        return refreshableDataFlow(
            dataChangeFlow = letterSrsManager.dataChangeFlow,
            lifecycleState = lifecycleState,
            valueProvider = { getStats(this) }
        )
    }

    private suspend fun getStats(coroutineScope: CoroutineScope): StatsData {
        Logger.logMethod()
        val today = timeUtils.getCurrentDate()

        return StatsData(
            todayStats = getDayStats(today),
            monthStats = refreshableStats(
                coroutineScope = coroutineScope,
                initialTimePeriod = YearMonth(today.year, today.month),
                statsProvider = { getMonthStats(it) }
            ),
            yearStats = refreshableStats(
                coroutineScope = coroutineScope,
                initialTimePeriod = today.year,
                statsProvider = { getYearStats(it) }
            ),
            totalStats = getTotalStats()
        )
    }

    private suspend fun getDayStats(date: LocalDate): DayStats {
        val stats = getStatsForPeriod(date, date.plus(1, DateTimeUnit.DAY))
        val reviews = stats.dateToReviewsMap.entries.firstOrNull()?.value ?: emptyList()
        return DayStats(
            date = date,
            reviews = reviews.size,
            timeSpent = reviews
                .map { it.duration }
                .fold(Duration.ZERO) { acc, duration ->
                    acc.plus(duration.coerceAtMost(SingleReviewDurationLimit))
                }
        )
    }

    private suspend fun <TimePeriod, Stats> refreshableStats(
        coroutineScope: CoroutineScope,
        initialTimePeriod: TimePeriod,
        statsProvider: suspend (TimePeriod) -> Stats
    ): RefreshableStats<TimePeriod, Stats> {

        val timePeriodState = mutableStateOf(initialTimePeriod)
        val statsState = mutableStateOf(statsProvider(initialTimePeriod))
        val isLoading = mutableStateOf(false)

        snapshotFlow { timePeriodState.value }
            .drop(1)
            .onEach {
                isLoading.value = true
                statsState.value = statsProvider(it)
                isLoading.value = false
            }
            .launchIn(coroutineScope)

        return RefreshableStats(
            currentTimePeriod = initialTimePeriod,
            selectedTimePeriodState = timePeriodState,
            isLoading = isLoading,
            statsState = statsState
        )
    }

    private suspend fun getMonthStats(month: YearMonth): TimePeriodStats {
        val monthStart = LocalDate(month.year, month.month, 1)
        val monthEnd = monthStart.plus(1, DateTimeUnit.MONTH)
        return getStatsForPeriod(monthStart, monthEnd)
    }

    private suspend fun getYearStats(year: Int): TimePeriodStats {
        val yearStart = LocalDate(year, 1, 1)
        val yearEnd = LocalDate(year + 1, 1, 1)
        return getStatsForPeriod(yearStart, yearEnd)

    }

    private suspend fun getTotalStats(): TotalStats {
        return TotalStats(
            reviews = reviewHistoryRepository
                .getTotalReviewsCount()
                .toInt(),
            timeSpent = reviewHistoryRepository
                .getTotalPracticeTime(SingleReviewDurationLimit.inWholeMilliseconds),
            uniqueLettersStudied = reviewHistoryRepository
                .getUniqueReviewItemsCount(LetterPracticeType.srsPracticeTypeValues)
                .toInt(),
            uniqueWordsStudied = reviewHistoryRepository
                .getUniqueReviewItemsCount(VocabPracticeType.srsPracticeTypeValues)
                .toInt()
        )
    }

    private suspend fun getStatsForPeriod(
        start: LocalDate,
        end: LocalDate
    ): TimePeriodStats {
        val timeZone = TimeZone.currentSystemDefault()
        val reviewsToDateMap = reviewHistoryRepository
            .getReviews(
                start.atStartOfDayIn(timeZone),
                end.atStartOfDayIn(timeZone)
            )
            .map { historyItem ->
                historyItem to historyItem.timestamp.toLocalDateTime(timeZone).date
            }

        val dateToReviewsMap = reviewsToDateMap
            .groupBy { (_, date) -> date }
            .toList()
            .associate { (date, items) ->
                date to items.map { (reviewHistoryItem, date) -> reviewHistoryItem }
            }

        return TimePeriodStats(
            dateToReviewsMap = dateToReviewsMap.mapValues { (_, practices) -> practices },
        )
    }

    companion object {
        private val SingleReviewDurationLimit = 1.minutes
    }

}