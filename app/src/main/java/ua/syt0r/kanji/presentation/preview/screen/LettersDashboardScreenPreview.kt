package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import ua.syt0r.kanji.core.srs.DailyLimitConfiguration
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.common.ui.kanji.PreviewKanji
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListMode
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckDashboardItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckStudyProgress
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.DailyIndicatorData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.DailyProgress
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenUI
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

private val dailyIndicatorData = DailyIndicatorData(
    configuration = DailyLimitConfiguration(true, 6, 12),
    progress = DailyProgress.Completed
)

private fun randomKanjiList(count: Int) = (0 until count).map { PreviewKanji.randomKanji() }

private fun randomStudyProgress(): LetterDeckStudyProgress {
    return LetterDeckStudyProgress(
        known = randomKanjiList(Random.nextInt(1, 6)),
        review = randomKanjiList(Random.nextInt(1, 6)),
        new = randomKanjiList(Random.nextInt(1, 30)),
        quickLearn = emptyList(),
        quickReview = emptyList(),
        all = emptyList()
    )
}

private fun getLoadedState(itemsCount: Int) = ScreenState.Loaded(
    listState = DeckDashboardListState(
        items = (0 until itemsCount).map {
            LetterDeckDashboardItem(
                id = Random.nextLong(),
                title = "Grade $it",
                position = 1,
                elapsedSinceLastReview = 1.days,
                writingProgress = randomStudyProgress(),
                readingProgress = randomStudyProgress()
            )
        },
        appliedSortByReviewTime = mutableStateOf(false),
        mode = mutableStateOf(DeckDashboardListMode.Browsing),
    ),
    dailyIndicatorData = dailyIndicatorData
)

@Composable
private fun ScreenPreview(
    state: ScreenState.Loaded = getLoadedState(10),
    useDarkTheme: Boolean = false,
) {
    AppTheme(useDarkTheme) {
        Surface {
            LettersDashboardScreenUI(
                state = rememberUpdatedState(newValue = state),
                navigateToDeckPicker = {},
                navigateToDeckDetails = {},
                startQuickPractice = { _, _, _ -> },
                navigateToDailyLimit = {},
                mergeDecks = { },
                sortDecks = { },
            )
        }
    }
}

@Preview
@Composable
private fun EmptyPreview() {
    ScreenPreview(
        state = getLoadedState(0)
    )
}

@Preview
@Composable
fun LightPreview() {
    ScreenPreview()
}

@Preview(device = Devices.PIXEL_C)
@Composable
private fun TabletDarkPreview() {
    ScreenPreview(useDarkTheme = true)
}