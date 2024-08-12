package ua.syt0r.kanji.core.user_data.practice.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.user_data.practice.CharacterStudyProgress
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterPracticeType

object UserDataDatabaseMigrationAfter3 {

    fun handleMigrations(driver: SqlDriver) {
        migrateCharacterProgress(
            sqlDriver = driver,
            readTable = "writing_review",
            practiceType = 0
        )
        migrateCharacterProgress(
            sqlDriver = driver,
            readTable = "reading_review",
            practiceType = 1
        )
    }

    private fun migrateCharacterProgress(
        sqlDriver: SqlDriver,
        readTable: String,
        practiceType: Int
    ) {
        val reviews = sqlDriver.executeQuery(
            identifier = null,
            sql = "SELECT * FROM $readTable",
            mapper = {
                val list = mutableListOf<Triple<String, Long, Long>>()
                while (it.next().value) {
                    val character = it.getString(0)!!
                    val timestamp = it.getLong(2)!!
                    val mistakes = it.getLong(3)!!
                    list.add(Triple(character, timestamp, mistakes))
                }
                QueryResult.Value(list)
            },
            parameters = 0
        ).value

        val characterProgresses = reviews.groupBy { it.first }
            .mapNotNull { (character, data) ->
                val lastReviewTime = data.maxOfOrNull { it.second }
                    ?: return@mapNotNull null

                val failedReviews = data.filter { it.third > 2 }
                val lastFailedReviewTime = failedReviews.maxOfOrNull { it.second } ?: 0

                val successfulReviewsAfterLastFailed =
                    data.filter { it.second >= lastFailedReviewTime }

                CharacterStudyProgress(
                    character = character,
                    practiceType = PreferencesLetterPracticeType.Writing,
                    lastReviewTime = Instant.fromEpochMilliseconds(lastReviewTime),
                    repeats = successfulReviewsAfterLastFailed.size,
                    lapses = failedReviews.size
                )
            }

        characterProgresses.forEach {
            it.apply {
                sqlDriver.execute(
                    identifier = null,
                    sql = "INSERT INTO character_progress(character, mode, last_review_time, repeats, lapses) VALUES('$character', $practiceType, ${lastReviewTime.toEpochMilliseconds()}, $repeats, $lapses)",
                    parameters = 0
                ).value
            }
        }
    }

}