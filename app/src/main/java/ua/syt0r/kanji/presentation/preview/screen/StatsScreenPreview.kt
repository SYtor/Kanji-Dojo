package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreenUI
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.use_case.StatsData
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private val fakeData = StatsScreenContract.ScreenState.Loaded(
    StatsData(
        today = LocalDate(2023, 11, 21),
        yearlyPractices = (0..40).associate {
            LocalDate(2023, Random.nextInt(1, 12), Random.nextInt(1, 27)) to
                    Random.nextInt(1, 10)
        },
        todayReviews = 4,
        todayTimeSpent = 205.seconds,
        totalReviews = 200,
        totalTimeSpent = 60004.seconds,
        uniqueLettersStudied = Random.nextInt(0, 200),
        uniqueWordsStudied = Random.nextInt(0, 200),
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
