package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case

import kotlinx.coroutines.flow.Flow
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.DashboardVocabDeck
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.vocabDecks

interface SubscribeOnDashboardVocabDecksUseCase {
    operator fun invoke(
        invalidationRequests: Flow<Unit>
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
        invalidationRequests: Flow<Unit>
    ): Flow<RefreshableData<VocabDecks>> {
        return refreshableDataFlow(
            dataChangeFlow = repository.changesFlow,
            invalidationRequestsFlow = invalidationRequests,
            provider = { getUpdatedDecks() }
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