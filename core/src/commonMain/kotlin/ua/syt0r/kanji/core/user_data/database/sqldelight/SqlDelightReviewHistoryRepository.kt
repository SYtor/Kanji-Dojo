package ua.syt0r.kanji.core.user_data.database.sqldelight

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryItem
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.database.StreakData
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseManager
import ua.syt0r.kanji.core.userdata.db.Review_history
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds


class SqlDelightReviewHistoryRepository(
    private val userDataDatabaseManager: UserDataDatabaseManager
) : ReviewHistoryRepository {

    override suspend fun addReview(
        item: ReviewHistoryItem
    ) = userDataDatabaseManager.writeTransaction {
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
    ): List<ReviewHistoryItem> = userDataDatabaseManager.readTransaction {
        getReviewHistory(start.toEpochMilliseconds(), end.toEpochMilliseconds())
            .executeAsList()
            .map { it.converted() }
    }

    override suspend fun getFirstReviewTime(
        key: String,
        practiceType: Long
    ): Instant? = userDataDatabaseManager.readTransaction {
        getFirstReview(key, practiceType)
            .executeAsOneOrNull()
            ?.converted()
            ?.timestamp
    }

    override suspend fun getDeckLastReview(
        deckId: Long,
        practiceTypes: List<Long>
    ): Instant? = userDataDatabaseManager.readTransaction {
        getLastDeckReview(deckId, practiceTypes).executeAsOneOrNull()?.MAX
            ?.let { Instant.fromEpochMilliseconds(it) }
    }

    override suspend fun getTotalReviewsCount(): Long = userDataDatabaseManager
        .readTransaction { getTotalReviewsCount().executeAsOne() }

    override suspend fun getUniqueReviewItemsCount(
        practiceTypes: List<Long>
    ): Long = userDataDatabaseManager.readTransaction {
        getUniqueReviewItemsCountForPracticeTypes(practiceTypes).executeAsOne()
    }

    override suspend fun getTotalPracticeTime(
        singleReviewDurationLimit: Long
    ): Duration = userDataDatabaseManager.readTransaction {
        getTotalReviewsDuration(singleReviewDurationLimit).executeAsOneOrNull()
            ?.SUM
            ?.milliseconds
            ?: Duration.ZERO
    }

    override suspend fun getStreaks() = userDataDatabaseManager.readTransaction {
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
