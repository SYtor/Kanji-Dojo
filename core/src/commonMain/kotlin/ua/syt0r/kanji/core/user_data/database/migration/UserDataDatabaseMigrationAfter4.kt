package ua.syt0r.kanji.core.user_data.database.migration

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver

object UserDataDatabaseMigrationAfter4 {

    fun handleMigrations(driver: SqlDriver) {
        fixMismatchedOutcomes(
            sqlDriver = driver,
            reviewTable = "writing_review"
        )
        fixMismatchedOutcomes(
            sqlDriver = driver,
            reviewTable = "reading_review"
        )
    }

    private class PracticeEntryData(
        val character: String,
        val timestamp: Long,
        val outcome: Long,
        val duration: Long
    )

    private fun fixMismatchedOutcomes(
        sqlDriver: SqlDriver,
        reviewTable: String
    ) {

        val reviews = sqlDriver.executeQuery(
            identifier = null,
            sql = "SELECT character, timestamp, outcome, duration FROM $reviewTable WHERE outcome NOT IN (0, 1);",
            mapper = {
                val list = mutableListOf<PracticeEntryData>()
                while (it.next().value) {
                    list.add(
                        PracticeEntryData(
                            character = it.getString(0)!!,
                            timestamp = it.getLong(1)!!,
                            outcome = it.getLong(2)!!,
                            duration = it.getLong(3)!!
                        )
                    )
                }
                QueryResult.Value(list)
            },
            parameters = 0
        ).value

        reviews.forEach {
            if (it.outcome == 0L || it.outcome == 1L) {
                return@forEach
            }

            sqlDriver.execute(
                identifier = null,
                sql = "UPDATE $reviewTable SET outcome=?, duration=? WHERE character=? AND timestamp=?;",
                parameters = 4,
                binders = {
                    // updating mismatched duration and outcome values
                    bindLong(0, it.duration.takeIf { it == 0L || it == 1L } ?: 1L)
                    bindLong(1, it.outcome)
                    bindString(2, it.character)
                    bindLong(3, it.timestamp)
                }
            )
        }
    }

}