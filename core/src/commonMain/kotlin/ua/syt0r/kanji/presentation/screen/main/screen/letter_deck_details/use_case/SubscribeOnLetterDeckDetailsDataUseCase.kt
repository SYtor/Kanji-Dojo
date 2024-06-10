package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.CharacterReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.PracticeItemSummary
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.toReviewState

interface SubscribeOnLetterDeckDetailsDataUseCase {
    operator fun invoke(
        practiceId: Long,
        screenShownEvents: Flow<Unit>,
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
) : SubscribeOnLetterDeckDetailsDataUseCase {

    override operator fun invoke(
        practiceId: Long,
        screenShownEvents: Flow<Unit>,
    ): Flow<RefreshableData<LetterDeckDetailsData>> {
        return refreshableDataFlow(
            dataChangeFlow = letterSrsManager.dataChangeFlow,
            invalidationRequestsFlow = screenShownEvents,
            provider = { getUpdatedData(practiceId) }
        )
    }

    private suspend fun getUpdatedData(
        practiceId: Long,
    ): LetterDeckDetailsData {
        Logger.logMethod()
        val appState = letterSrsManager.getUpdatedData()

        val deckInfo = appState.decks.find { it.id == practiceId }!!
        val timeZone = TimeZone.currentSystemDefault()

        val items = deckInfo.characters.mapIndexed { index, character ->
            val characterProgress = appState.characterProgresses[character]

            val lastWritingReviewDate = characterProgress?.writingProgress?.lastReviewTime
            val expectedWritingReviewTime = characterProgress?.writingProgress
                ?.getExpectedReviewTime(1.1f)
                ?.toLocalDateTime(timeZone)


            val lastReadingReviewDate = characterProgress?.readingProgress?.lastReviewTime
            val expectedReadingReviewTime = characterProgress?.readingProgress
                ?.getExpectedReviewTime(1.1f)
                ?.toLocalDateTime(timeZone)


            LetterDeckDetailsItemData(
                character = character,
                positionInPractice = index,
                frequency = appDataRepository.getData(character)?.frequency,
                writingSummary = PracticeItemSummary(
                    firstReviewDate = practiceRepository
                        .getFirstReviewTime(character, PracticeType.Writing)
                        ?.toLocalDateTime(timeZone),
                    lastReviewDate = lastWritingReviewDate?.toLocalDateTime(timeZone),
                    expectedReviewDate = expectedWritingReviewTime,
                    lapses = characterProgress?.writingProgress?.lapses ?: 0,
                    repeats = characterProgress?.writingProgress?.repeats ?: 0,
                    state = characterProgress?.writingStatus?.toReviewState()
                        ?: CharacterReviewState.New
                ),
                readingSummary = PracticeItemSummary(
                    firstReviewDate = practiceRepository
                        .getFirstReviewTime(character, PracticeType.Reading)
                        ?.toLocalDateTime(timeZone),
                    lastReviewDate = lastReadingReviewDate?.toLocalDateTime(timeZone),
                    expectedReviewDate = expectedReadingReviewTime,
                    lapses = characterProgress?.readingProgress?.lapses ?: 0,
                    repeats = characterProgress?.readingProgress?.repeats ?: 0,
                    state = characterProgress?.readingStatus?.toReviewState()
                        ?: CharacterReviewState.New
                )
            )
        }

        return LetterDeckDetailsData(
            deckTitle = deckInfo.title,
            items = items,
            sharePractice = items.joinToString("") { it.character }
        )

    }

}