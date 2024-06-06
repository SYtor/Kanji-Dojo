package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.zip
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.DashboardVocabDeck
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.vocabDecks

interface GetVocabDecksUseCase {
    operator fun invoke(
        invalidationRequests: Flow<Unit>
    ): Flow<RefreshableData<VocabDecks>>
}

data class VocabDecks(
    val userDecks: List<DashboardVocabDeck>,
    val defaultDecks: List<DashboardVocabDeck>
)

class DefaultGetVocabDecksUseCase(
    private val repository: VocabPracticeRepository
) : GetVocabDecksUseCase {

    override fun invoke(
        invalidationRequests: Flow<Unit>
    ): Flow<RefreshableData<VocabDecks>> = flow {

        val dataChangesFlow = listOf(flowOf(Unit), repository.changesFlow).merge()

        dataChangesFlow.zip(invalidationRequests) { _, _ -> }
            .collect {
                emit(RefreshableData.Loading())
                emit(RefreshableData.Loaded(getUpdatedDecks()))
            }

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