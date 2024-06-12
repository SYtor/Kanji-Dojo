package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeType
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.PracticeItemSummary
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.toReviewState

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
) : SubscribeOnLetterDeckDetailsDataUseCase {

    override operator fun invoke(
        deckId: Long,
        lifecycleState: StateFlow<LifecycleState>,
    ): Flow<RefreshableData<LetterDeckDetailsData>> {
        return refreshableDataFlow(
            dataChangeFlow = letterSrsManager.dataChangeFlow,
            lifecycleState = lifecycleState,
            valueProvider = { getUpdatedData(deckId) }
        )
    }

    private suspend fun getUpdatedData(deckId: Long): LetterDeckDetailsData {
        Logger.logMethod()

        val deckInfo = letterSrsManager.getUpdatedDeckInfo(deckId)
        val timeZone = TimeZone.currentSystemDefault()

        val items = deckInfo.characters.mapIndexed { index, character ->
            val writingData = deckInfo.writingDetails.all.first { it.character == character }
            val readingData = deckInfo.readingDetails.all.first { it.character == character }

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

        return LetterDeckDetailsData(
            deckTitle = deckInfo.title,
            items = items,
            sharePractice = items.joinToString("") { it.character }
        )

    }

}