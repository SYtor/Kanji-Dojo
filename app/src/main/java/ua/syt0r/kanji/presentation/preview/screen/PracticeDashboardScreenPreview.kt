package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.common.ui.kanji.PreviewKanji
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.DailyIndicatorData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.DailyProgress
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardListMode
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeStudyProgress
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.ui.PracticeDashboardScreenUI
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

private val dailyIndicatorData = DailyIndicatorData(
    configuration = DailyGoalConfiguration(true, 6, 12),
    progress = DailyProgress.Completed
)

private fun randomKanjiList(count: Int) = (0 until count).map { PreviewKanji.randomKanji() }

private fun randomStudyProgress(): PracticeStudyProgress {
    return PracticeStudyProgress(
        known = randomKanjiList(Random.nextInt(1, 6)),
        review = randomKanjiList(Random.nextInt(1, 6)),
        new = randomKanjiList(Random.nextInt(1, 30)),
        quickLearn = emptyList(),
        quickReview = emptyList(),
        all = emptyList()
    )
}

@Preview
@Composable
private fun EmptyPreview(
    state: ScreenState = ScreenState.Loaded(
        mode = MutableStateFlow(
            PracticeDashboardListMode.Default(emptyList())
        ),
        dailyIndicatorData = dailyIndicatorData
    ),
    useDarkTheme: Boolean = false,
) {
    AppTheme(useDarkTheme) {
        PracticeDashboardScreenUI(
            state = rememberUpdatedState(newValue = state),
            navigateToImportPractice = {},
            navigateToCreatePractice = {},
            navigateToPracticeDetails = {},
            startQuickPractice = {},
            updateDailyGoalConfiguration = {},
            startMerge = { },
            merge = { },
            startReorder = { },
            reorder = { },
            enableDefaultMode = { },
        )
    }
}

@Preview
@Composable
fun PracticeDashboardUIPreview() {
    EmptyPreview(
        state = ScreenState.Loaded(
            mode = (1..5).map {
                PracticeDashboardItem(
                    practiceId = Random.nextLong(),
                    title = "Grade $it",
                    position = 1,
                    timeSinceLastPractice = 1.days,
                    writingProgress = randomStudyProgress(),
                    readingProgress = randomStudyProgress()
                )
            }.let { MutableStateFlow(PracticeDashboardListMode.Default(it)) },
            dailyIndicatorData = dailyIndicatorData
        )
    )
}

@Preview(device = Devices.PIXEL_C)
@Composable
private fun TabletPreview() {
    EmptyPreview(
        state = ScreenState.Loaded(
            mode = (0..10).map {
                PracticeDashboardItem(
                    practiceId = Random.nextLong(),
                    title = "Grade $it",
                    position = 1,
                    timeSinceLastPractice = if (it % 2 == 0) null else it.days,
                    writingProgress = randomStudyProgress(),
                    readingProgress = randomStudyProgress()
                )
            }.let { MutableStateFlow(PracticeDashboardListMode.Default(it)) },
            dailyIndicatorData = dailyIndicatorData
        ),
        useDarkTheme = true
    )
}