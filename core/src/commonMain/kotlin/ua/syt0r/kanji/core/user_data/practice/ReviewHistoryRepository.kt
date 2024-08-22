package ua.syt0r.kanji.core.user_data.practice

import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.user_data.practice.db.UserDataDatabaseManager
import ua.syt0r.kanji.core.userdata.db.Review_history
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

interface ReviewHistoryRepository {
    suspend fun addReview(item: ReviewHistoryItem)
    suspend fun getReviews(start: Instant, end: Instant): List<ReviewHistoryItem>
    suspend fun getFirstReviewTime(key: String, practiceType: Long): Instant?
    suspend fun getTotalReviewsCount(): Long
    suspend fun getTotalReviewsCount(practiceTypes: List<Long>): Long
    suspend fun getTotalPracticeTime(singleReviewDurationLimit: Long): Duration
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

class SqlDelightReviewHistoryRepository(
    private val userDataDatabaseManager: UserDataDatabaseManager
) : ReviewHistoryRepository {

    override suspend fun addReview(
        item: ReviewHistoryItem
    ) = userDataDatabaseManager.runTransaction {
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
    ): List<ReviewHistoryItem> = userDataDatabaseManager.runTransaction {
        getReviewHistory(start.toEpochMilliseconds(), end.toEpochMilliseconds())
            .executeAsList()
            .map { it.converted() }
    }

    override suspend fun getFirstReviewTime(
        key: String,
        practiceType: Long
    ): Instant? = userDataDatabaseManager.runTransaction {
        getFirstReview(key, practiceType)
            .executeAsOneOrNull()
            ?.converted()
            ?.timestamp
    }

    override suspend fun getTotalReviewsCount(): Long = userDataDatabaseManager.runTransaction {
        getTotalReviewsCount().executeAsOne()
    }

    override suspend fun getTotalReviewsCount(
        practiceTypes: List<Long>
    ): Long = userDataDatabaseManager.runTransaction {
        getTotalReviewsCountForPracticeTypes(practiceTypes).executeAsOne()
    }

    override suspend fun getTotalPracticeTime(
        singleReviewDurationLimit: Long
    ): Duration = userDataDatabaseManager.runTransaction {
        getTotalReviewsDuration(singleReviewDurationLimit).executeAsOneOrNull()
            ?.SUM
            ?.milliseconds
            ?: Duration.ZERO
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