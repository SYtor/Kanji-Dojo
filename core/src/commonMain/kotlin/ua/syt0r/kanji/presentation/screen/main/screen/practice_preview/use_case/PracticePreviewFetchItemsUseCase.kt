package ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.use_case

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.app_state.AppStateManager
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.PracticePreviewScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.CharacterReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticeItemSummary
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticePreviewItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.toReviewState

class PracticePreviewFetchItemsUseCase(
    private val appStateManager: AppStateManager,
    private val appDataRepository: AppDataRepository,
    private val practiceRepository: LetterPracticeRepository
) : PracticePreviewScreenContract.FetchItemsUseCase {

    override suspend fun fetch(
        practiceId: Long
    ): List<PracticePreviewItem> {
        val appState = appStateManager.appStateFlow.filter { !it.isLoading }
            .first()
            .lastData!!

        val deckInfo = appState.decks.find { it.id == practiceId }!!
        val timeZone = TimeZone.currentSystemDefault()

        return deckInfo.characters.mapIndexed { index, character ->
            val characterProgress = appState.characterProgresses[character]

            val lastWritingReviewDate = characterProgress?.writingProgress?.lastReviewTime
            val expectedWritingReviewTime = characterProgress?.writingProgress
                ?.getExpectedReviewTime(1.1f)
                ?.toLocalDateTime(timeZone)


            val lastReadingReviewDate = characterProgress?.readingProgress?.lastReviewTime
            val expectedReadingReviewTime = characterProgress?.readingProgress
                ?.getExpectedReviewTime(1.1f)
                ?.toLocalDateTime(timeZone)


            PracticePreviewItem(
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

    }

}