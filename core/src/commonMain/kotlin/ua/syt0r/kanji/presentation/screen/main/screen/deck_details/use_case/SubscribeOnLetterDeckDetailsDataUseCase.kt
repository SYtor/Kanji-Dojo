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
import ua.syt0r.kanji.core.srs.CharacterSrsData
import ua.syt0r.kanji.core.srs.LetterSrsDeckInfo
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeType
import ua.syt0r.kanji.presentation.LifecycleState
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
    private val practiceRepository: LetterPracticeRepository,
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

        val deckInfo: LetterSrsDeckInfo
        val writingMap: Map<String, CharacterSrsData>
        val readingMap: Map<String, CharacterSrsData>

        val timeToGetDeckInfo = measureTimeMillis {
            deckInfo = letterSrsManager.getUpdatedDeckInfo(deckId)
            writingMap = deckInfo.writingDetails.charactersData
            readingMap = deckInfo.readingDetails.charactersData
        }
        Logger.d("timeToGetDeckInfo[$timeToGetDeckInfo]")

        val timeZone = TimeZone.currentSystemDefault()

        val items = deckInfo.characters.mapIndexed { index, character ->
            val writingData = writingMap.getValue(character)
            val readingData = readingMap.getValue(character)

            DeckDetailsItemData.LetterData(
                character = character,
                positionInPractice = index,
                frequency = appDataRepository.getData(character)?.frequency,
                writingSummary = PracticeItemSummary(
                    firstReviewDate = practiceRepository
                        .getFirstReviewTime(character, PracticeType.Writing)
                        ?.toLocalDateTime(timeZone),
                    lastReviewDate = writingData.studyProgress?.lastReviewTime
                        ?.toLocalDateTime(timeZone),
                    expectedReviewDate = writingData.expectedReviewDate,
                    lapses = writingData.studyProgress?.lapses ?: 0,
                    repeats = writingData.studyProgress?.repeats ?: 0,
                    srsItemStatus = writingData.status
                ),
                readingSummary = PracticeItemSummary(
                    firstReviewDate = practiceRepository
                        .getFirstReviewTime(character, PracticeType.Reading)
                        ?.toLocalDateTime(timeZone),
                    lastReviewDate = readingData.studyProgress?.lastReviewTime
                        ?.toLocalDateTime(timeZone),
                    expectedReviewDate = readingData.expectedReviewDate,
                    lapses = readingData.studyProgress?.lapses ?: 0,
                    repeats = readingData.studyProgress?.repeats ?: 0,
                    srsItemStatus = readingData.status
                )
            )
        }

        DeckDetailsData.LetterDeckData(
            deckTitle = deckInfo.title,
            items = items,
            sharableDeckData = items.joinToString("") { it.character }
        )

    }

}