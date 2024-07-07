package ua.syt0r.kanji.core.srs.use_case

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.core.srs.CharacterSrsData
import ua.syt0r.kanji.core.srs.CharacterStudyProgressCache
import ua.syt0r.kanji.core.user_data.practice.CharacterStudyProgress
import ua.syt0r.kanji.core.user_data.preferences.PracticeType

interface GetLetterSrsStatusUseCase {

    suspend operator fun invoke(
        letter: String,
        practiceType: PracticeType,
        date: LocalDate,
    ): CharacterSrsData

}

class DefaultGetLetterSrsStatusUseCase(
    private val characterStudyProgressCache: CharacterStudyProgressCache
) : GetLetterSrsStatusUseCase {

    override suspend fun invoke(
        letter: String,
        practiceType: PracticeType,
        date: LocalDate,
    ): CharacterSrsData {
        val studyProgress: CharacterStudyProgress? = characterStudyProgressCache.get(letter)
            .find { it.practiceType == practiceType }
        val expectedReviewDate = studyProgress?.getExpectedReviewTime(DEFAULT_SRS_INTERVAL)
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?.date
        val status = when {
            expectedReviewDate == null -> SrsItemStatus.New
            expectedReviewDate > date -> SrsItemStatus.Done
            else -> SrsItemStatus.Review
        }
        return CharacterSrsData(letter, status, expectedReviewDate, studyProgress)
    }

    companion object {
        private const val DEFAULT_SRS_INTERVAL = 1.1f
    }

}