package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryItem
import kotlin.time.Duration


data class StatsData(
    val todayStats: DayStats,
    val monthStats: RefreshableStats<YearMonth, TimePeriodStats>,
    val yearStats: RefreshableStats<Int, TimePeriodStats>,
    val totalStats: TotalStats
)

data class DayStats(
    val date: LocalDate,
    val reviews: Int,
    val timeSpent: Duration
)

data class YearMonth(
    val monthStart: LocalDate
) {

    val year = monthStart.year
    val month = monthStart.month

    constructor(year: Int, month: Month) : this(LocalDate(year, month, 1))

}

data class RefreshableStats<TimePeriod, StatsData>(
    val currentTimePeriod: TimePeriod,
    val selectedTimePeriodState: MutableState<TimePeriod>,
    val isLoading: State<Boolean>,
    val statsState: State<StatsData>
)

data class TimePeriodStats(
    val dateToReviewsMap: Map<LocalDate, List<ReviewHistoryItem>>
)

data class TotalStats(
    val reviews: Int,
    val timeSpent: Duration,
    val uniqueLettersStudied: Int,
    val uniqueWordsStudied: Int
)