package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.DailyLimitConfiguration
import ua.syt0r.kanji.core.srs.DeckStudyProgress
import ua.syt0r.kanji.core.srs.LetterSrsDeckInfo
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckDashboardItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckStudyProgress
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.DailyIndicatorData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.DailyProgress
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenData

class LettersDashboardLoadDataUseCase(
    private val srsManager: LetterSrsManager,
    private val timeUtils: TimeUtils,
) : LettersDashboardScreenContract.LoadDataUseCase {

    override fun load(
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<LettersDashboardScreenData>> {
        return refreshableDataFlow(
            dataChangeFlow = srsManager.dataChangeFlow,
            lifecycleState = lifecycleState,
            valueProvider = { getUpdatedScreenData() }
        )
    }

    private suspend fun getUpdatedScreenData(): LettersDashboardScreenData {
        Logger.logMethod()
        val srsDecksData = srsManager.getUpdatedDecksData()
        val time = timeUtils.now()

        return LettersDashboardScreenData(
            items = srsDecksData.decks
                .map { deckInfo ->
                    val writingProgress = deckInfo.writingDetails.toPracticeStudyProgress(
                        configuration = srsDecksData.dailyLimitConfiguration,
                        leftToStudy = srsDecksData.dailyProgress.newLeft,
                        leftToReview = srsDecksData.dailyProgress.dueLeft
                    )
                    val readingProgress = deckInfo.readingDetails.toPracticeStudyProgress(
                        configuration = srsDecksData.dailyLimitConfiguration,
                        leftToStudy = srsDecksData.dailyProgress.newLeft,
                        leftToReview = srsDecksData.dailyProgress.dueLeft
                    )
                    LetterDeckDashboardItem(
                        id = deckInfo.id,
                        title = deckInfo.title,
                        position = deckInfo.position,
                        elapsedSinceLastReview = deckInfo.getLastReviewTime()?.let { time - it },
                        writingProgress = writingProgress,
                        readingProgress = readingProgress
                    )
                },
            dailyIndicatorData = DailyIndicatorData(
                configuration = srsDecksData.dailyLimitConfiguration,
                progress = getDailyProgress(
                    configuration = srsDecksData.dailyLimitConfiguration,
                    leftToStudy = srsDecksData.dailyProgress.newLeft,
                    leftToReview = srsDecksData.dailyProgress.dueLeft
                )
            )
        )
    }

    private fun DeckStudyProgress.toPracticeStudyProgress(
        configuration: DailyLimitConfiguration,
        leftToStudy: Int,
        leftToReview: Int,
    ): LetterDeckStudyProgress {
        return LetterDeckStudyProgress(
            all = all,
            known = done,
            review = due,
            new = new,
            quickLearn = if (configuration.enabled) new.take(leftToStudy) else new,
            quickReview = if (configuration.enabled) due.take(leftToReview) else due
        )
    }

    private fun getDailyProgress(
        configuration: DailyLimitConfiguration,
        leftToStudy: Int,
        leftToReview: Int,
    ): DailyProgress {
        return when {
            !configuration.enabled -> DailyProgress.Disabled
            leftToStudy > 0 && leftToReview > 0 -> DailyProgress.StudyAndReview(
                leftToStudy,
                leftToReview
            )

            leftToStudy == 0 && leftToReview > 0 -> DailyProgress.ReviewOnly(leftToReview)
            leftToStudy > 0 && leftToReview == 0 -> DailyProgress.StudyOnly(leftToStudy)
            else -> DailyProgress.Completed
        }
    }

    private fun LetterSrsDeckInfo.getLastReviewTime(): Instant? {
        val reviewTimes = writingDetails.charactersData
            .mapNotNull { it.value.studyProgress?.lastReviewTime } +
                readingDetails.charactersData
                    .mapNotNull { it.value.studyProgress?.lastReviewTime }
        return reviewTimes.maxOrNull()
    }

}
