package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import ua.syt0r.kanji.core.srs.DeckStudyProgress
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.DailyIndicatorData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.DailyProgress
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardScreenData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeStudyProgress

class PracticeDashboardLoadDataUseCase(
    private val srsManager: LetterSrsManager,
    private val timeUtils: TimeUtils,
) : PracticeDashboardScreenContract.LoadDataUseCase {

    override fun load(
        screenVisibilityEvents: Flow<Unit>,
        preferencesChangeEvents: Flow<Unit>,
    ): Flow<RefreshableData<PracticeDashboardScreenData>> {
        return refreshableDataFlow(
            dataChangeFlow = srsManager.dataChangeFlow,
            invalidationRequestsFlow = merge(screenVisibilityEvents, preferencesChangeEvents),
            provider = { getUpdatedScreenData() }
        )
    }

    private suspend fun getUpdatedScreenData(): PracticeDashboardScreenData {
        Logger.logMethod()
        val srsDecksData = srsManager.getUpdatedDecksData()
        val time = timeUtils.now()

        return PracticeDashboardScreenData(
            items = srsDecksData.decks
                .map { deckInfo ->
                    PracticeDashboardItem(
                        practiceId = deckInfo.id,
                        title = deckInfo.title,
                        position = deckInfo.position,
                        timeSinceLastPractice = deckInfo.lastReviewTime?.let { time - it },
                        writingProgress = deckInfo.writingDetails.toPracticeStudyProgress(
                            configuration = srsDecksData.dailyGoalConfiguration,
                            leftToStudy = srsDecksData.dailyProgress.leftToStudy,
                            leftToReview = srsDecksData.dailyProgress.leftToReview
                        ),
                        readingProgress = deckInfo.readingDetails.toPracticeStudyProgress(
                            configuration = srsDecksData.dailyGoalConfiguration,
                            leftToStudy = srsDecksData.dailyProgress.leftToStudy,
                            leftToReview = srsDecksData.dailyProgress.leftToReview
                        )
                    )
                },
            dailyIndicatorData = DailyIndicatorData(
                configuration = srsDecksData.dailyGoalConfiguration,
                progress = getDailyProgress(
                    configuration = srsDecksData.dailyGoalConfiguration,
                    leftToStudy = srsDecksData.dailyProgress.leftToStudy,
                    leftToReview = srsDecksData.dailyProgress.leftToReview
                )
            )
        )
    }

    private fun DeckStudyProgress.toPracticeStudyProgress(
        configuration: DailyGoalConfiguration,
        leftToStudy: Int,
        leftToReview: Int,
    ): PracticeStudyProgress {
        return PracticeStudyProgress(
            all = all.map { it.character },
            known = done,
            review = review,
            new = new,
            quickLearn = if (configuration.enabled) new.take(leftToStudy) else new,
            quickReview = if (configuration.enabled) review.take(leftToReview) else review
        )
    }

    private fun getDailyProgress(
        configuration: DailyGoalConfiguration,
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

}
