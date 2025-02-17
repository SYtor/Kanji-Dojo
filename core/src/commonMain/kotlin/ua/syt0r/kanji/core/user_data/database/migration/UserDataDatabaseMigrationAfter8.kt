package ua.syt0r.kanji.core.user_data.database.migration

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.fsrs.DefaultFsrsScheduler
import ua.syt0r.kanji.core.srs.fsrs.Fsrs5
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams

object UserDataDatabaseMigrationAfter8 {

    private class MigrationReviewHistoryItem(
        val key: String,
        val practiceType: Long,
        val timestamp: Instant,
        val grade: Int,
    )

    fun handleMigrations(sqlDriver: SqlDriver) {
        val items = sqlDriver.executeQuery(
            identifier = null,
            sql = """
                SELECT key, practice_type, timestamp, grade 
                FROM review_history 
                WHERE practice_type IN (0, 1);
            """.trimIndent(),
            mapper = {
                val list = mutableListOf<MigrationReviewHistoryItem>()
                while (it.next().value) {
                    list.add(
                        MigrationReviewHistoryItem(
                            key = it.getString(0)!!,
                            practiceType = it.getLong(1)!!,
                            timestamp = Instant.fromEpochMilliseconds(it.getLong(2)!!),
                            grade = it.getLong(3)!!.toInt(),
                        )
                    )
                }
                QueryResult.Value(list)
            },
            parameters = 0
        ).value

        val srsScheduler = DefaultFsrsScheduler(Fsrs5())

        val fsrsCards = items
            .groupBy { it.key to it.practiceType }
            .map { (groupKey, groupItems) ->
                val (key, practiceType) = groupKey
                val srsCard = groupItems.sortedBy { it.timestamp }
                    .fold(srsScheduler.newCard()) { fsrsCard, historyItem ->
                        val answers = srsScheduler.schedule(fsrsCard, historyItem.timestamp)
                        when (historyItem.grade) {
                            1 -> answers.again
                            2 -> answers.hard
                            3 -> answers.good
                            4 -> answers.easy
                            else -> error("unsupported grade")
                        }
                    }
                SrsCardKey(key, practiceType) to srsCard
            }

        fsrsCards.forEach { (key, card) ->
            sqlDriver.execute(
                identifier = null,
                sql = """
                    INSERT INTO fsrs_card(key,practice_type,status,stability,difficulty,lapses,repeats,last_review,interval)
                    VALUES (?,?,?,?,?,?,?,?,?);
                """.trimIndent(),
                parameters = 9,
                binders = {
                    card.params as FsrsCardParams.Existing
                    bindString(0, key.itemKey)
                    bindLong(1, key.practiceType)
                    bindLong(2, card.status.ordinal.toLong())
                    bindDouble(3, card.params.stability)
                    bindDouble(4, card.params.difficulty)
                    bindLong(5, card.lapses.toLong())
                    bindLong(6, card.repeats.toLong())
                    bindLong(7, card.lastReview!!.toEpochMilliseconds())
                    bindLong(8, card.interval.inWholeMilliseconds)
                }
            )
        }

    }

}