package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.VocabSrsDeckProgress
import ua.syt0r.kanji.core.srs.VocabSrsManager
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.VocabDeckDashboardItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.VocabDeckStudyProgress

interface SubscribeOnDashboardVocabDecksUseCase {
    operator fun invoke(
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<VocabDashboardScreenData>>
}

data class VocabDashboardScreenData(
    val decks: List<VocabDeckDashboardItem>,
    val dailyLimitEnabled: Boolean
)

class DefaultSubscribeOnDashboardVocabDecksUseCase(
    private val vocabSrsManager: VocabSrsManager,
    private val timeUtils: TimeUtils
) : SubscribeOnDashboardVocabDecksUseCase {

    override fun invoke(
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<VocabDashboardScreenData>> {
        return refreshableDataFlow(
            dataChangeFlow = vocabSrsManager.dataChangeFlow,
            lifecycleState = lifecycleState,
            valueProvider = { getUpdatedDecks() }
        )
    }

    private suspend fun getUpdatedDecks(): VocabDashboardScreenData {
        Logger.logMethod()
        val decksData = vocabSrsManager.getDecks()
        val decks = decksData.decks
        val now = timeUtils.now()
        return VocabDashboardScreenData(
            decks = decks.map {
                VocabDeckDashboardItem(
                    deckId = it.id,
                    title = it.title,
                    position = it.position,
                    elapsedSinceLastReview = it.lastReview?.let { now.minus(it) },
                    studyProgress = it.progressMap
                        .map { (practiceType, srsProgress) ->
                            ScreenVocabPracticeType.from(practiceType) to srsProgress.toStudyProgress()
                        }
                        .toMap()
                )
            },
            dailyLimitEnabled = decksData.dailyLimitEnabled
        )
    }

    private fun VocabSrsDeckProgress.toStudyProgress(): VocabDeckStudyProgress {
        return VocabDeckStudyProgress(
            all = itemsData.keys.toList(),
            completed = done,
            due = due,
            new = new,
            dailyNew = new,
            dailyDue = due
        )
    }

}