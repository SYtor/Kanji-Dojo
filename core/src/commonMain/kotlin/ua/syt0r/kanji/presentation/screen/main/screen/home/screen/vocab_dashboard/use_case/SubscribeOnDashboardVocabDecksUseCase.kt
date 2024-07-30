package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.VocabSrsManager
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.DashboardVocabDeck

interface SubscribeOnDashboardVocabDecksUseCase {
    operator fun invoke(
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<VocabDecks>>
}

data class VocabDecks(
    val userDecks: List<DashboardVocabDeck.User>,
    val defaultDecks: List<DashboardVocabDeck.Default>
)

class DefaultSubscribeOnDashboardVocabDecksUseCase(
    private val vocabSrsManager: VocabSrsManager
) : SubscribeOnDashboardVocabDecksUseCase {

    override fun invoke(
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<VocabDecks>> {
        return refreshableDataFlow(
            dataChangeFlow = vocabSrsManager.dataChangeFlow,
            lifecycleState = lifecycleState,
            valueProvider = { getUpdatedDecks() }
        )
    }

    private suspend fun getUpdatedDecks(): VocabDecks {
        Logger.logMethod()
        val decks = vocabSrsManager.getUpdatedDecksData().decks
        return VocabDecks(
            userDecks = decks.map {
                DashboardVocabDeck.User(
                    titleResolver = { it.title },
                    words = it.summaries.values.first().all,
                    srsProgress = it.summaries,
                    id = it.id
                )
            },
            defaultDecks = emptyList()
        )
    }

}