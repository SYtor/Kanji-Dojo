package ua.syt0r.kanji.core.user_data.practice

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterPracticeType
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

interface LetterPracticeRepository {

    val changesFlow: SharedFlow<Unit>

    suspend fun createPractice(title: String, characters: List<String>)
    suspend fun createPracticeAndMerge(title: String, practiceIdToMerge: List<Long>)
    suspend fun updatePracticePositions(practiceIdToPositionMap: Map<Long, Int>)
    suspend fun deletePractice(id: Long)
    suspend fun updatePractice(
        id: Long,
        title: String,
        charactersToAdd: List<String>,
        charactersToRemove: List<String>,
    )

    suspend fun getAllPractices(): List<Practice>
    suspend fun getPracticeInfo(id: Long): Practice

    suspend fun getKanjiForPractice(id: Long): List<String>

    suspend fun saveWritingReviews(
        practiceTime: Instant,
        reviewResultList: List<CharacterWritingReviewResult>,
    )

    suspend fun saveReadingReviews(
        practiceTime: Instant,
        reviewResultList: List<CharacterReadingReviewResult>,
    )

    suspend fun getFirstReviewTime(character: String, type: PreferencesLetterPracticeType): Instant?
    suspend fun getLastReviewTime(practiceId: Long, type: PreferencesLetterPracticeType): Instant?
    suspend fun getStudyProgress(character: String, type: PreferencesLetterPracticeType): CharacterStudyProgress?
    suspend fun getStudyProgresses(): List<CharacterStudyProgress>

    suspend fun getReviews(start: Instant, end: Instant): Map<CharacterReviewResult, Instant>
    suspend fun getTotalReviewsCount(): Long
    suspend fun getTotalPracticeTime(singleReviewDurationLimit: Long): Long
    suspend fun getTotalUniqueReviewedCharactersCount(): Long

}

data class Practice(
    val id: Long,
    val name: String,
    val position: Int,
)

data class CharacterStudyProgress(
    val character: String,
    val practiceType: PreferencesLetterPracticeType,
    val lastReviewTime: Instant,
    val repeats: Int,
    val lapses: Int,
) {

    fun getExpectedReviewTime(srsInterval: Float): Instant {
        val additionalMillis = srsInterval * 1.days.toLong(DurationUnit.MILLISECONDS) * repeats
        return Instant.fromEpochMilliseconds(lastReviewTime.toEpochMilliseconds() + additionalMillis.roundToLong())
    }

}

enum class CharacterReviewOutcome { Success, Fail }

interface CharacterReviewResult {
    val character: String
    val practiceId: Long
    val mistakes: Int
    val reviewDuration: Duration
    val outcome: CharacterReviewOutcome
}

data class CharacterWritingReviewResult(
    override val character: String,
    override val practiceId: Long,
    override val mistakes: Int,
    override val reviewDuration: Duration,
    override val outcome: CharacterReviewOutcome,
    val isStudy: Boolean,
) : CharacterReviewResult

data class CharacterReadingReviewResult(
    override val character: String,
    override val practiceId: Long,
    override val mistakes: Int,
    override val reviewDuration: Duration,
    override val outcome: CharacterReviewOutcome,
) : CharacterReviewResult

