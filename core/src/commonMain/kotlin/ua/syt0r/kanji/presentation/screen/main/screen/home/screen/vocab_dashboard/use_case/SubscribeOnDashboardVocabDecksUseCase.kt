package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.DashboardVocabDeck
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.vocabDecks

interface SubscribeOnDashboardVocabDecksUseCase {
    operator fun invoke(
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<VocabDecks>>
}

data class VocabDecks(
    val userDecks: List<DashboardVocabDeck>,
    val defaultDecks: List<DashboardVocabDeck>
)

class DefaultSubscribeOnDashboardVocabDecksUseCase(
    private val repository: VocabPracticeRepository
) : SubscribeOnDashboardVocabDecksUseCase {

    override fun invoke(
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<VocabDecks>> {
        return refreshableDataFlow(
            dataChangeFlow = repository.changesFlow,
            lifecycleState = lifecycleState,
            valueProvider = { getUpdatedDecks() }
        )
    }

    private suspend fun getUpdatedDecks(): VocabDecks {
        Logger.logMethod()
        return VocabDecks(
            userDecks = repository.getDecks().map {
                DashboardVocabDeck(
                    titleResolver = { it.title },
                    expressionIds = repository.getDeckWords(it.id),
                    id = it.id
                )
            },
            defaultDecks = vocabDecks
        )
    }

}