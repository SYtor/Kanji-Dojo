package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case

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
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.PracticeItemSummary
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.toReviewState
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

interface SubscribeOnLetterDeckDetailsDataUseCase {
    operator fun invoke(
        deckId: Long,
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<LetterDeckDetailsData>>
}

data class LetterDeckDetailsData(
    val deckTitle: String,
    val items: List<LetterDeckDetailsItemData>,
    val sharePractice: String,
)

class DefaultSubscribeOnLetterDeckDetailsDataUseCase(
    private val letterSrsManager: LetterSrsManager,
    private val appDataRepository: AppDataRepository,
    private val practiceRepository: LetterPracticeRepository,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : SubscribeOnLetterDeckDetailsDataUseCase {

    override operator fun invoke(
        deckId: Long,
        lifecycleState: StateFlow<LifecycleState>,
    ): Flow<RefreshableData<LetterDeckDetailsData>> {
        return refreshableDataFlow(
            dataChangeFlow = letterSrsManager.dataChangeFlow,
            lifecycleState = lifecycleState,
            valueProvider = {
                var data: LetterDeckDetailsData
                val timeToRefreshData = measureTimeMillis { data = getUpdatedData(deckId) }
                Logger.d("timeToRefreshData[$timeToRefreshData]")
                data
            }
        )
    }

    private suspend fun getUpdatedData(
        deckId: Long
    ): LetterDeckDetailsData = withContext(coroutineContext) {
        Logger.logMethod()

        val deckInfo: LetterSrsDeckInfo
        val writingMap: Map<String, CharacterSrsData>
        val readingMap: Map<String, CharacterSrsData>

        val timeToGetDeckInfo = measureTimeMillis {
            deckInfo = letterSrsManager.getUpdatedDeckInfo(deckId)
            writingMap = deckInfo.writingDetails.all.associateBy { it.character }
            readingMap = deckInfo.readingDetails.all.associateBy { it.character }
        }
        Logger.d("timeToGetDeckInfo[$timeToGetDeckInfo]")

        val timeZone = TimeZone.currentSystemDefault()

        val items = deckInfo.characters.mapIndexed { index, character ->
            val writingData = writingMap.getValue(character)
            val readingData = readingMap.getValue(character)

            LetterDeckDetailsItemData(
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
                    state = writingData.status.toReviewState()
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
                    state = readingData.status.toReviewState()
                )
            )
        }

        LetterDeckDetailsData(
            deckTitle = deckInfo.title,
            items = items,
            sharePractice = items.joinToString("") { it.character }
        )

    }

}