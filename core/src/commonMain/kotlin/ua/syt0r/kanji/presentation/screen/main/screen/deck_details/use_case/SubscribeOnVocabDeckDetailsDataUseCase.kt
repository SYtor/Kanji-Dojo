package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.VocabSrsManager
import ua.syt0r.kanji.core.user_data.database.VocabPracticeRepository
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsScreenConfiguration
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

interface SubscribeOnVocabDeckDetailsDataUseCase {
    operator fun invoke(
        configuration: DeckDetailsScreenConfiguration.VocabDeck,
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<DeckDetailsData.VocabDeckData>>
}

class DefaultSubscribeOnVocabDeckDetailsDataUseCase(
    private val vocabSrsManager: VocabSrsManager,
    private val vocabPracticeRepository: VocabPracticeRepository,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : SubscribeOnVocabDeckDetailsDataUseCase {

    override operator fun invoke(
        configuration: DeckDetailsScreenConfiguration.VocabDeck,
        lifecycleState: StateFlow<LifecycleState>,
    ): Flow<RefreshableData<DeckDetailsData.VocabDeckData>> {
        return refreshableDataFlow(
            dataChangeFlow = merge(
                vocabSrsManager.dataChangeFlow,
                vocabPracticeRepository.changesFlow
            ),
            lifecycleState = lifecycleState,
            valueProvider = {
                var data: DeckDetailsData.VocabDeckData
                val timeToRefreshData = measureTimeMillis { data = getUpdatedData(configuration) }
                Logger.d("timeToRefreshData[$timeToRefreshData]")
                data
            }
        )
    }

    private suspend fun getUpdatedData(
        configuration: DeckDetailsScreenConfiguration.VocabDeck
    ): DeckDetailsData.VocabDeckData = withContext(coroutineContext) {
        val deckInfo = vocabSrsManager.getDeck(configuration.deckId)
        val vocabCards = vocabPracticeRepository.getAllCards()
            .associateBy { it.cardId }

        DeckDetailsData.VocabDeckData(
            deckTitle = deckInfo.title,
            items = deckInfo.items.mapIndexed { index, cardId ->
                DeckDetailsItemData.VocabData(
                    card = vocabCards.getValue(cardId),
                    positionInPractice = index,
                    srsStatus = ScreenVocabPracticeType.entries.associateWith {
                        deckInfo.progressMap.getValue(it.dataType).itemsData
                            .getValue(cardId).status
                    }
                )
            }
        )
    }

}