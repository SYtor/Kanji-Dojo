package ua.syt0r.kanji.core.srs.use_case

import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.core.srs.CharacterProgressStatus
import ua.syt0r.kanji.core.srs.DeckStudyProgress
import ua.syt0r.kanji.core.srs.LetterSrsDeckInfo
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeType

interface GetLetterDeckSrsProgressUseCase {
    suspend operator fun invoke(deckId: Long, date: LocalDate): LetterSrsDeckInfo
}

class DefaultGetLetterDeckSrsProgressUseCase(
    private val repository: LetterPracticeRepository,
    private val getLetterSrsStatusUseCase: GetLetterSrsStatusUseCase,
) : GetLetterDeckSrsProgressUseCase {

    override suspend operator fun invoke(deckId: Long, date: LocalDate): LetterSrsDeckInfo {
        val deckInfo = repository.getPracticeInfo(deckId)
        val characters = repository.getKanjiForPractice(deckId)

        val lastWritingReviewTime = repository.getLastReviewTime(
            practiceId = deckId,
            type = PracticeType.Writing
        )

        val lastReadingReviewTime = repository.getLastReviewTime(
            practiceId = deckId,
            type = PracticeType.Reading
        )

        val reviewTime = listOfNotNull(lastWritingReviewTime, lastReadingReviewTime).maxOrNull()

        return LetterSrsDeckInfo(
            id = deckId,
            title = deckInfo.name,
            position = deckInfo.position,
            characters = characters,
            lastReviewTime = reviewTime,
            writingDetails = getProgressForPracticeType(
                characters = characters,
                practiceType = PracticeType.Writing,
                date = date
            ),
            readingDetails = getProgressForPracticeType(
                characters = characters,
                practiceType = PracticeType.Reading,
                date = date
            )
        )
    }

    private suspend fun getProgressForPracticeType(
        characters: List<String>,
        practiceType: PracticeType,
        date: LocalDate,
    ): DeckStudyProgress {
        val charactersSrsData = characters.map { getLetterSrsStatusUseCase(it, practiceType, date) }
        val new = charactersSrsData.filter { it.status == CharacterProgressStatus.New }
            .map { it.character }
        val due = charactersSrsData.filter { it.status == CharacterProgressStatus.Review }
            .map { it.character }
        val done = charactersSrsData.filter { it.status == CharacterProgressStatus.Done }
            .map { it.character }

        return DeckStudyProgress(
            all = charactersSrsData,
            done = done.toList(),
            review = due.toList(),
            new = new.toList()
        )
    }

}