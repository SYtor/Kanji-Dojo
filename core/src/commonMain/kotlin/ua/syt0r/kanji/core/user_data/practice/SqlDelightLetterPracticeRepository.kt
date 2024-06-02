package ua.syt0r.kanji.core.user_data.practice

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.user_data.preferences.PracticeType
import ua.syt0r.kanji.core.user_data.practice.db.UserDataDatabaseManager
import ua.syt0r.kanji.core.userdata.db.Character_progress
import ua.syt0r.kanji.core.userdata.db.Reading_review
import ua.syt0r.kanji.core.userdata.db.Writing_review
import kotlin.time.Duration.Companion.milliseconds

class SqlDelightLetterPracticeRepository(
    private val databaseManager: UserDataDatabaseManager
) : LetterPracticeRepository {

    override val practiceChangeFlow: Flow<Unit> = databaseManager.dataChangedFlow

    override suspend fun createPractice(
        title: String,
        characters: List<String>
    ) = databaseManager.runTransaction(notifyDataChange = true) {
        insertPractice(name = title)

        val practiceId = getLastInsertRowId().executeAsOne()
        characters.forEach { insertOrIgnorePracticeEntry(it, practiceId) }
    }

    override suspend fun createPracticeAndMerge(
        title: String,
        practiceIdToMerge: List<Long>
    ) = databaseManager.runTransaction(notifyDataChange = true) {
        insertPractice(name = title)
        val practiceId = getLastInsertRowId().executeAsOne()

        migratePracticeEntries(practiceId, practiceIdToMerge)
        migrateWritingReviewsHistory(practiceId, practiceIdToMerge)
        migrateReadingReviewsHistory(practiceId, practiceIdToMerge)

        deletePractices(practiceIdToMerge)
    }

    override suspend fun updatePracticePositions(
        practiceIdToPositionMap: Map<Long, Int>
    ) = databaseManager.runTransaction(notifyDataChange = true) {
        practiceIdToPositionMap.forEach { (practiceId, position) ->
            updatePracticePosition(position.toLong(), practiceId)
        }
    }

    override suspend fun deletePractice(id: Long) = databaseManager.runTransaction(
        notifyDataChange = true
    ) {
        deletePractice(id)
    }

    override suspend fun updatePractice(
        id: Long,
        title: String,
        charactersToAdd: List<String>,
        charactersToRemove: List<String>
    ) = databaseManager.runTransaction(notifyDataChange = true) {
        updatePracticeTitle(title, id)
        charactersToAdd.forEach { insertOrIgnorePracticeEntry(it, id) }
        charactersToRemove.forEach { deletePracticeEntry(id, it) }
    }

    override suspend fun getAllPractices(): List<Practice> = databaseManager.runTransaction {
        getAllPractices().executeAsList().map {
            Practice(it.id, it.name, it.position.toInt())
        }
    }

    override suspend fun getPracticeInfo(
        id: Long
    ): Practice = databaseManager.runTransaction {
        getPractice(id).executeAsOne().run { Practice(id, name, position.toInt()) }
    }

    override suspend fun getKanjiForPractice(
        id: Long
    ): List<String> = databaseManager.runTransaction {
        getPracticeEntriesForPractice(id).executeAsList().map { it.character }
    }

    override suspend fun saveWritingReviews(
        practiceTime: Instant,
        reviewResultList: List<CharacterWritingReviewResult>,
    ) = databaseManager.runTransaction(notifyDataChange = true) {
        val mode = practiceTypeToDBValue.getValue(PracticeType.Writing).toLong()
        reviewResultList.forEach {
            val currentProgress = getCharacterProgress(it.character, mode).executeAsOneOrNull()
                ?: Character_progress(
                    character = it.character,
                    mode = mode,
                    last_review_time = null,
                    repeats = 0,
                    lapses = 0
                )

            val updatedProgress = currentProgress.run {
                copy(
                    last_review_time = practiceTime.toEpochMilliseconds(),
                    repeats = if (it.outcome == CharacterReviewOutcome.Success) repeats + 1 else 1,
                    lapses = if (it.outcome == CharacterReviewOutcome.Success) lapses else lapses + 1
                )
            }

            updatedProgress.apply {
                upsertCharacterProgress(
                    character = character,
                    mode = mode,
                    last_review_time = last_review_time,
                    repeats = repeats,
                    lapses = lapses
                )
            }

            upsertWritingReview(
                character = it.character,
                practice_id = it.practiceId,
                timestamp = practiceTime.toEpochMilliseconds(),
                mistakes = it.mistakes.toLong(),
                is_study = if (it.isStudy) 1 else 0,
                duration = it.reviewDuration.inWholeMilliseconds,
                outcome = it.outcome.toLong()
            )
        }
    }

    override suspend fun saveReadingReviews(
        practiceTime: Instant,
        reviewResultList: List<CharacterReadingReviewResult>
    ) = databaseManager.runTransaction(notifyDataChange = true) {
        val mode = practiceTypeToDBValue.getValue(PracticeType.Reading).toLong()
        reviewResultList.forEach {
            val currentProgress = getCharacterProgress(it.character, mode).executeAsOneOrNull()
                ?: Character_progress(
                    character = it.character,
                    mode = mode,
                    last_review_time = null,
                    repeats = 0,
                    lapses = 0
                )

            val updatedProgress = currentProgress.run {
                copy(
                    last_review_time = practiceTime.toEpochMilliseconds(),
                    repeats = if (it.outcome == CharacterReviewOutcome.Success) repeats + 1 else 1,
                    lapses = if (it.outcome == CharacterReviewOutcome.Success) lapses else lapses + 1
                )
            }

            updatedProgress.apply {
                upsertCharacterProgress(
                    character = character,
                    mode = mode,
                    last_review_time = last_review_time,
                    repeats = repeats,
                    lapses = lapses
                )
            }

            upsertReadingReview(
                character = it.character,
                practice_id = it.practiceId,
                timestamp = practiceTime.toEpochMilliseconds(),
                mistakes = it.mistakes.toLong(),
                duration = it.reviewDuration.inWholeMilliseconds,
                outcome = it.outcome.toLong()
            )
        }
    }

    override suspend fun getFirstReviewTime(
        character: String,
        type: PracticeType
    ): Instant? = databaseManager.runTransaction {
        val timestamp = when (type) {
            PracticeType.Writing -> getFirstWritingReview(character).executeAsOneOrNull()?.timestamp
            PracticeType.Reading -> getFirstReadingReview(character).executeAsOneOrNull()?.timestamp
        }
        timestamp?.let { Instant.fromEpochMilliseconds(it) }
    }

    override suspend fun getLastReviewTime(
        practiceId: Long,
        type: PracticeType
    ): Instant? = databaseManager.runTransaction {
        val timestamp = when (type) {
            PracticeType.Writing -> getLastWritingReview(practiceId).executeAsOneOrNull()?.timestamp
            PracticeType.Reading -> getLastReadingReview(practiceId).executeAsOneOrNull()?.timestamp
        }
        timestamp?.let { Instant.fromEpochMilliseconds(it) }
    }

    override suspend fun getStudyProgresses(): List<CharacterStudyProgress> = databaseManager
        .runTransaction {
            getCharacterProgresses()
                .executeAsList()
                .map { dbItem ->
                    CharacterStudyProgress(
                        character = dbItem.character,
                        practiceType = practiceTypeToDBValue.entries
                            .first { dbItem.mode == it.value }.key,
                        lastReviewTime = Instant.fromEpochMilliseconds(dbItem.last_review_time!!),
                        repeats = dbItem.repeats.toInt(),
                        lapses = dbItem.lapses.toInt()
                    )
                }
        }

    override suspend fun getReviews(
        start: Instant,
        end: Instant
    ): Map<CharacterReviewResult, Instant> = databaseManager.runTransaction {
        val writingReviews = getWritingReviews(
            start.toEpochMilliseconds(),
            end.toEpochMilliseconds()
        )
            .executeAsList()
            .associate { it.converted() to Instant.fromEpochMilliseconds(it.timestamp) }

        val readingReviews = getReadingReviews(
            start.toEpochMilliseconds(),
            end.toEpochMilliseconds()
        )
            .executeAsList()
            .associate { it.converted() to Instant.fromEpochMilliseconds(it.timestamp) }

        writingReviews + readingReviews
    }

    override suspend fun getTotalReviewsCount(): Long = databaseManager.runTransaction {
        getTotalWritingReviewsCount().executeAsOne() + getTotalReadingReviewsCount().executeAsOne()
    }

    override suspend fun getTotalPracticeTime(
        singleReviewDurationLimit: Long
    ): Long = databaseManager.runTransaction {
        val writingsDuration = getTotalWritingReviewsDuration(
            reviewDurationLimit = singleReviewDurationLimit
        ).executeAsOne().SUM?.toLong() ?: 0L
        val readingsDuration = getTotalReadingReviewsDuration(
            reviewDurationLimit = singleReviewDurationLimit
        ).executeAsOne().SUM?.toLong() ?: 0L
        writingsDuration + readingsDuration
    }


    override suspend fun getTotalUniqueReviewedCharactersCount(): Long =
        databaseManager.runTransaction { getTotalUniqueReviewedCharactersCount().executeAsOne() }

    private fun CharacterReviewOutcome.toLong(): Long = when (this) {
        CharacterReviewOutcome.Success -> 1
        CharacterReviewOutcome.Fail -> 0
    }

    private fun Writing_review.converted(): CharacterWritingReviewResult {
        return CharacterWritingReviewResult(
            character = character,
            practiceId = practice_id,
            mistakes = mistakes.toInt(),
            reviewDuration = duration.milliseconds,
            outcome = parseOutcome(outcome),
            isStudy = is_study == 1L
        )
    }

    private fun Reading_review.converted(): CharacterReadingReviewResult {
        return CharacterReadingReviewResult(
            character = character,
            practiceId = practice_id,
            mistakes = mistakes.toInt(),
            reviewDuration = duration.milliseconds,
            outcome = parseOutcome(outcome)
        )
    }

    private fun parseOutcome(value: Long): CharacterReviewOutcome = when (value) {
        1L -> CharacterReviewOutcome.Success
        0L -> CharacterReviewOutcome.Fail
        else -> throw IllegalStateException("Unknown outcome $value")
    }

    private val practiceTypeToDBValue = mapOf(
        PracticeType.Writing to 0L,
        PracticeType.Reading to 1L
    )

}

