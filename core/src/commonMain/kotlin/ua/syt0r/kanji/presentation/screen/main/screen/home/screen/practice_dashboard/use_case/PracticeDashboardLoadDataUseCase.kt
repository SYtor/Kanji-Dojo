package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import ua.syt0r.kanji.core.srs.DeckStudyProgress
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.DailyIndicatorData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.DailyProgress
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardScreenData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeStudyProgress
import kotlin.math.max
import kotlin.math.min

class PracticeDashboardLoadDataUseCase(
    private val srsManager: LetterSrsManager
) : PracticeDashboardScreenContract.LoadDataUseCase {

    override fun load(
        screenVisibilityEvents: Flow<Unit>,
        preferencesChangeEvents: Flow<Unit>
    ): Flow<RefreshableData<PracticeDashboardScreenData>> {
        return refreshableDataFlow(
            dataChangeFlow = srsManager.dataChangeFlow,
            invalidationRequestsFlow = merge(screenVisibilityEvents, preferencesChangeEvents),
            provider = { getUpdatedScreenData() }
        )
    }

    private suspend fun getUpdatedScreenData(): PracticeDashboardScreenData {
        Logger.logMethod()
        val appState = srsManager.getUpdatedData()

        val configuration = appState.dailyGoalConfiguration
        val progress = appState.dailyProgress

        val totalNew = appState.decks.flatMap { it.writingDetails.new }.distinct().size +
                appState.decks.flatMap { it.readingDetails.new }.distinct().size

        val totalReview = appState.decks.flatMap { it.writingDetails.review }.distinct()
            .size + appState.decks.flatMap { it.readingDetails.review }.distinct().size

        val leftToStudy = max(
            a = 0,
            b = min(configuration.learnLimit - progress.studied, totalNew)
        )

        val leftToReview = max(
            a = 0,
            b = min(configuration.reviewLimit - progress.reviewed, totalReview)
        )

        return PracticeDashboardScreenData(
            items = appState.decks
                .map { deckInfo ->
                    PracticeDashboardItem(
                        practiceId = deckInfo.id,
                        title = deckInfo.title,
                        position = deckInfo.position,
                        timeSinceLastPractice = deckInfo.timeSinceLastReview,
                        writingProgress = deckInfo.writingDetails.toPracticeStudyProgress(
                            configuration = appState.dailyGoalConfiguration,
                            leftToStudy = leftToStudy,
                            leftToReview = leftToReview
                        ),
                        readingProgress = deckInfo.readingDetails.toPracticeStudyProgress(
                            configuration = appState.dailyGoalConfiguration,
                            leftToStudy = leftToStudy,
                            leftToReview = leftToReview
                        )
                    )
                },
            dailyIndicatorData = DailyIndicatorData(
                configuration = appState.dailyGoalConfiguration,
                progress = getDailyProgress(
                    configuration = appState.dailyGoalConfiguration,
                    leftToStudy = leftToStudy,
                    leftToReview = leftToReview
                )
            )
        )
    }

    private fun DeckStudyProgress.toPracticeStudyProgress(
        configuration: DailyGoalConfiguration,
        leftToStudy: Int,
        leftToReview: Int
    ): PracticeStudyProgress {
        return PracticeStudyProgress(
            all = all,
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
        leftToReview: Int
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
