package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.VocabSrsManager
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsScreenConfiguration
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
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
    private val appDataRepository: AppDataRepository,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : SubscribeOnVocabDeckDetailsDataUseCase {

    override operator fun invoke(
        configuration: DeckDetailsScreenConfiguration.VocabDeck,
        lifecycleState: StateFlow<LifecycleState>,
    ): Flow<RefreshableData<DeckDetailsData.VocabDeckData>> {
        return refreshableDataFlow(
            dataChangeFlow = vocabSrsManager.dataChangeFlow,
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
        val deckInfo = vocabSrsManager.getUpdatedDeckInfo(configuration.deckId)
        DeckDetailsData.VocabDeckData(
            deckTitle = deckInfo.title,
            items = deckInfo.words.mapIndexed { index, wordId ->
                DeckDetailsItemData.VocabData(
                    word = appDataRepository.getWord(wordId),
                    positionInPractice = index,
                    srsStatus = ScreenVocabPracticeType.values().associateWith {
                        deckInfo.summaries.getValue(it.dataType)
                            .wordsData.getValue(wordId).status
                    }
                )
            }
        )
    }

}