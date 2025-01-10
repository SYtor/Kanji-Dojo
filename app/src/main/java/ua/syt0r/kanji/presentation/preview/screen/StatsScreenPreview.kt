package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.DayStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.RefreshableStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreenUI
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.TimePeriodStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.TotalStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.YearMonth
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val fakeData = StatsScreenContract.ScreenState.Loaded(
    StatsData(
        todayStats = DayStats(
            date = LocalDate(2023, 11, 21),
            reviews = 4,
            timeSpent = 5.minutes
        ),
        monthStats = RefreshableStats(
            currentTimePeriod = YearMonth(2023, Month.NOVEMBER),
            selectedTimePeriodState = mutableStateOf(YearMonth(2023, Month.NOVEMBER)),
            isLoading = mutableStateOf(false),
            statsState = mutableStateOf(
                TimePeriodStats(
                    dateToReviewsMap = emptyMap()
                )
            )
        ),
        yearStats = RefreshableStats(
            currentTimePeriod = 2023,
            selectedTimePeriodState = mutableStateOf(2023),
            isLoading = mutableStateOf(false),
            statsState = mutableStateOf(
                TimePeriodStats(
                    dateToReviewsMap = emptyMap()
                )
            )
        ),
        totalStats = TotalStats(
            reviews = 200,
            timeSpent = 1.hours,
            uniqueLettersStudied = Random.nextInt(0, 200),
            uniqueWordsStudied = Random.nextInt(0, 200)
        )
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ScreenPreview() {
    AppTheme {
        StatsScreenUI(rememberUpdatedState(fakeData))
    }
}

@Preview(showSystemUi = true, device = Devices.PIXEL_C)
@Composable
private fun TabletPreview() {
    ScreenPreview()
}
