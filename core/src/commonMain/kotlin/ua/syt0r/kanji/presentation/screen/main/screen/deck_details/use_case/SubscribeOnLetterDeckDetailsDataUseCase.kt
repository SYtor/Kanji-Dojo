package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.LetterSrsDeck
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.srs.SrsCardData
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.PracticeItemSummary
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

interface SubscribeOnDeckDetailsDataUseCase {
    operator fun invoke(
        configuration: DeckDetailsScreenConfiguration.LetterDeck,
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<DeckDetailsData.LetterDeckData>>
}

class DefaultSubscribeOnDeckDetailsDataUseCase(
    private val letterSrsManager: LetterSrsManager,
    private val appDataRepository: AppDataRepository,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : SubscribeOnDeckDetailsDataUseCase {

    override operator fun invoke(
        configuration: DeckDetailsScreenConfiguration.LetterDeck,
        lifecycleState: StateFlow<LifecycleState>,
    ): Flow<RefreshableData<DeckDetailsData.LetterDeckData>> {
        val deckId = configuration.deckId
        return refreshableDataFlow(
            dataChangeFlow = letterSrsManager.dataChangeFlow,
            lifecycleState = lifecycleState,
            valueProvider = {
                var data: DeckDetailsData.LetterDeckData
                val timeToRefreshData = measureTimeMillis { data = getUpdatedData(deckId) }
                Logger.d("timeToRefreshData[$timeToRefreshData]")
                data
            }
        )
    }

    private suspend fun getUpdatedData(
        deckId: Long
    ): DeckDetailsData.LetterDeckData = withContext(coroutineContext) {
        Logger.logMethod()

        val deck: LetterSrsDeck
        val writingMap: Map<String, SrsCardData>
        val readingMap: Map<String, SrsCardData>

        val timeToGetDeckInfo = measureTimeMillis {
            deck = letterSrsManager.getDeck(deckId)
            writingMap = deck.progressMap.getValue(LetterPracticeType.Writing).itemsData
            readingMap = deck.progressMap.getValue(LetterPracticeType.Reading).itemsData
        }
        Logger.d("timeToGetDeckInfo[$timeToGetDeckInfo]")

        val timeZone = TimeZone.currentSystemDefault()

        val items = deck.items.mapIndexed { index, character ->

            val writingCardData = writingMap.getValue(character)
            val writingSummary = PracticeItemSummary(
                firstReviewDate = writingCardData.firstReview?.toLocalDateTime(timeZone),
                lastReviewDate = writingCardData.lastReview?.toLocalDateTime(timeZone),
                expectedReviewDate = writingCardData.expectedReviewDate,
                lapses = writingCardData.lapses,
                repeats = writingCardData.repeats,
                srsItemStatus = writingCardData.status
            )

            val readingCardData = readingMap.getValue(character)
            val readingSummary = PracticeItemSummary(
                firstReviewDate = readingCardData.firstReview?.toLocalDateTime(timeZone),
                lastReviewDate = readingCardData.lastReview?.toLocalDateTime(timeZone),
                expectedReviewDate = readingCardData.expectedReviewDate,
                lapses = readingCardData.lapses,
                repeats = readingCardData.repeats,
                srsItemStatus = readingCardData.status
            )

            DeckDetailsItemData.LetterData(
                character = character,
                positionInPractice = index,
                frequency = appDataRepository.getData(character)?.frequency,
                summaryMap = mapOf(
                    ScreenLetterPracticeType.Writing to writingSummary,
                    ScreenLetterPracticeType.Reading to readingSummary
                )
            )
        }

        DeckDetailsData.LetterDeckData(
            deckTitle = deck.title,
            items = items,
            sharableDeckData = items.joinToString("") { it.character }
        )

    }

}