package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.LetterSrsDeckProgress
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckDashboardItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckStudyProgress
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
        val srsDecksData = srsManager.getDecks()
        val currentInstant = timeUtils.now()
        return LettersDashboardScreenData(
            items = srsDecksData.decks.map { deck ->
                val writingProgress = deck.progressMap
                    .getValue(LetterPracticeType.Writing)
                    .toPracticeStudyProgress()
                val readingProgress = deck.progressMap
                    .getValue(LetterPracticeType.Reading)
                    .toPracticeStudyProgress()
                LetterDeckDashboardItem(
                    deckId = deck.id,
                    title = deck.title,
                    position = deck.position,
                    elapsedSinceLastReview = deck.lastReview?.let { currentInstant - it },
                    writingProgress = writingProgress,
                    readingProgress = readingProgress
                )
            },
            dailyLimitEnabled = srsDecksData.dailyLimitEnabled
        )
    }

    private fun LetterSrsDeckProgress.toPracticeStudyProgress(): LetterDeckStudyProgress {
        return LetterDeckStudyProgress(
            all = itemsData.keys.toList(),
            completed = done,
            due = due,
            new = new,
            dailyNew = dailyNew,
            dailyDue = dailyDue
        )
    }

}
