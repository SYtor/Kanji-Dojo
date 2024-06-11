package ua.syt0r.kanji.core.srs.use_case

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.srs.CharacterProgressStatus
import ua.syt0r.kanji.core.srs.CharacterSrsData
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeType

interface GetLetterSrsStatusUseCase {

    suspend operator fun invoke(
        letter: String,
        practiceType: PracticeType,
        date: LocalDate,
    ): CharacterSrsData

}

class DefaultGetLetterSrsStatusUseCase(
    private val repository: LetterPracticeRepository,
) : GetLetterSrsStatusUseCase {

    override suspend fun invoke(
        letter: String,
        practiceType: PracticeType,
        date: LocalDate,
    ): CharacterSrsData {
        val studyProgress = repository.getStudyProgress(letter, practiceType)
        val expectedReviewDate = studyProgress?.getExpectedReviewTime(DEFAULT_SRS_INTERVAL)
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?.date
        val status = when {
            expectedReviewDate == null -> CharacterProgressStatus.New
            expectedReviewDate > date -> CharacterProgressStatus.Done
            else -> CharacterProgressStatus.Review
        }
        return CharacterSrsData(letter, status, expectedReviewDate, studyProgress)
    }

    companion object {
        private const val DEFAULT_SRS_INTERVAL = 1.1f
    }

}