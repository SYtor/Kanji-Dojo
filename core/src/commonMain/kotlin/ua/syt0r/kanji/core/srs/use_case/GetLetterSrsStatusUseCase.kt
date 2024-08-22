package ua.syt0r.kanji.core.srs.use_case

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.srs.CharacterSrsData
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.SrsItemRepository
import ua.syt0r.kanji.core.user_data.practice.CharacterStudyProgress

interface GetLetterSrsStatusUseCase {

    suspend operator fun invoke(
        letter: String,
        practiceType: LetterPracticeType,
        date: LocalDate,
    ): CharacterSrsData

}

class DefaultGetLetterSrsStatusUseCase(
    private val srsItemRepository: SrsItemRepository,
    private val getSrsStatusUseCase: GetSrsStatusUseCase
) : GetLetterSrsStatusUseCase {

    override suspend fun invoke(
        letter: String,
        practiceType: LetterPracticeType,
        date: LocalDate,
    ): CharacterSrsData {
        val srsCard = srsItemRepository.get(practiceType.toSrsKey(letter))
        val expectedReviewDate = srsCard?.lastReview?.plus(srsCard.interval)
        val status = getSrsStatusUseCase(expectedReviewDate)

        return CharacterSrsData(
            character = letter,
            status = status,
            expectedReviewDate = expectedReviewDate
                ?.toLocalDateTime(TimeZone.currentSystemDefault())
                ?.date,
            studyProgress = srsCard?.lastReview?.let {
                CharacterStudyProgress(
                    character = letter,
                    practiceType = practiceType,
                    lastReviewTime = srsCard.lastReview,
                    repeats = srsCard.fsrsCard.repeats,
                    lapses = srsCard.fsrsCard.lapses
                )
            }
        )
    }

}