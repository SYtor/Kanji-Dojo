package ua.syt0r.kanji.core.user_data.practice

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.core.user_data.practice.db.UserDataDatabaseManager
import ua.syt0r.kanji.core.userdata.db.Review_history
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface ReviewHistoryRepository {
    suspend fun addReview(item: ReviewHistoryItem)
    suspend fun getReviews(start: Instant, end: Instant): List<ReviewHistoryItem>
    suspend fun getFirstReviewTime(key: String, practiceType: Long): Instant?
    suspend fun getDeckLastReview(deckId: Long, practiceTypes: List<Long>): Instant?
    suspend fun getTotalReviewsCount(): Long
    suspend fun getUniqueReviewItemsCount(practiceTypes: List<Long>): Long
    suspend fun getTotalPracticeTime(singleReviewDurationLimit: Long): Duration
    suspend fun getStreaks(): List<StreakData>
}

data class ReviewHistoryItem(
    val key: String,
    val practiceType: Long,
    val timestamp: Instant,
    val duration: Duration,
    val grade: Int,
    val mistakes: Int,
    val deckId: Long,
)

class StreakData(
    val start: LocalDate,
    val end: LocalDate,
    val length: Int
)

class SqlDelightReviewHistoryRepository(
    private val userDataDatabaseManager: UserDataDatabaseManager
) : ReviewHistoryRepository {

    override suspend fun addReview(
        item: ReviewHistoryItem
    ) = userDataDatabaseManager.runTransaction(true) {
        item.run {
            upsertReview(
                key = key,
                practice_type = practiceType,
                timestamp = timestamp.toEpochMilliseconds(),
                duration = duration.inWholeMilliseconds,
                grade = grade.toLong(),
                mistakes = mistakes.toLong(),
                deck_id = deckId
            )
        }
    }

    override suspend fun getReviews(
        start: Instant,
        end: Instant
    ): List<ReviewHistoryItem> = userDataDatabaseManager.runTransaction(false) {
        getReviewHistory(start.toEpochMilliseconds(), end.toEpochMilliseconds())
            .executeAsList()
            .map { it.converted() }
    }

    override suspend fun getFirstReviewTime(
        key: String,
        practiceType: Long
    ): Instant? = userDataDatabaseManager.runTransaction(false) {
        getFirstReview(key, practiceType)
            .executeAsOneOrNull()
            ?.converted()
            ?.timestamp
    }

    override suspend fun getDeckLastReview(
        deckId: Long,
        practiceTypes: List<Long>
    ): Instant? = userDataDatabaseManager.runTransaction(false) {
        getLastDeckReview(deckId, practiceTypes).executeAsOneOrNull()?.MAX
            ?.let { Instant.fromEpochMilliseconds(it) }
    }

    override suspend fun getTotalReviewsCount(): Long = userDataDatabaseManager
        .runTransaction(false) { getTotalReviewsCount().executeAsOne() }

    override suspend fun getUniqueReviewItemsCount(
        practiceTypes: List<Long>
    ): Long = userDataDatabaseManager.runTransaction(false) {
        getUniqueReviewItemsCountForPracticeTypes(practiceTypes).executeAsOne()
    }

    override suspend fun getTotalPracticeTime(
        singleReviewDurationLimit: Long
    ): Duration = userDataDatabaseManager.runTransaction(false) {
        getTotalReviewsDuration(singleReviewDurationLimit).executeAsOneOrNull()
            ?.SUM
            ?.milliseconds
            ?: Duration.ZERO
    }

    override suspend fun getStreaks() = userDataDatabaseManager.runTransaction(false) {
        getReviewStreaks().executeAsList()
            .map {
                StreakData(
                    start = LocalDate.parse(it.start_date!!),
                    end = LocalDate.parse(it.end_date!!),
                    length = it.sequence_length.toInt()
                )
            }
    }

    private fun Review_history.converted(): ReviewHistoryItem = ReviewHistoryItem(
        key = key,
        practiceType = practice_type,
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        duration = duration.milliseconds,
        grade = grade.toInt(),
        mistakes = mistakes.toInt(),
        deckId = deck_id
    )

}