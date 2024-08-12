package ua.syt0r.kanji.core.user_data.practice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.mergeSharedFlows
import ua.syt0r.kanji.core.user_data.practice.db.UserDataDatabaseManager
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterPracticeType
import ua.syt0r.kanji.core.userdata.db.Character_progress
import ua.syt0r.kanji.core.userdata.db.PracticeQueries
import ua.syt0r.kanji.core.userdata.db.Reading_review
import ua.syt0r.kanji.core.userdata.db.Writing_review
import kotlin.time.Duration.Companion.milliseconds

class SqlDelightLetterPracticeRepository(
    private val databaseManager: UserDataDatabaseManager,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) : LetterPracticeRepository {

    private val _changesFlow = MutableSharedFlow<Unit>()
    override val changesFlow: SharedFlow<Unit> = mergeSharedFlows(
        coroutineScope,
        _changesFlow,
        databaseManager.databaseChangeFlow
    )

    private suspend fun <T> runTransaction(
        notifyDataChange: Boolean = false,
        block: PracticeQueries.() -> T,
    ): T {
        val result = databaseManager.runTransaction { block() }
        if (notifyDataChange) _changesFlow.emit(Unit)
        return result
    }

    override suspend fun createPractice(
        title: String,
        characters: List<String>,
    ) = runTransaction(notifyDataChange = true) {
        insertPractice(name = title)

        val practiceId = getLastInsertRowId().executeAsOne()
        characters.forEach { insertOrIgnorePracticeEntry(it, practiceId) }
    }

    override suspend fun createPracticeAndMerge(
        title: String,
        practiceIdToMerge: List<Long>,
    ) = runTransaction(notifyDataChange = true) {
        insertPractice(name = title)
        val practiceId = getLastInsertRowId().executeAsOne()

        migratePracticeEntries(practiceId, practiceIdToMerge)
        migrateWritingReviewsHistory(practiceId, practiceIdToMerge)
        migrateReadingReviewsHistory(practiceId, practiceIdToMerge)

        deletePractices(practiceIdToMerge)
    }

    override suspend fun updatePracticePositions(
        practiceIdToPositionMap: Map<Long, Int>,
    ) = runTransaction(notifyDataChange = true) {
        practiceIdToPositionMap.forEach { (practiceId, position) ->
            updatePracticePosition(position.toLong(), practiceId)
        }
    }

    override suspend fun deletePractice(id: Long) = runTransaction(
        notifyDataChange = true
    ) {
        deletePractice(id)
    }

    override suspend fun updatePractice(
        id: Long,
        title: String,
        charactersToAdd: List<String>,
        charactersToRemove: List<String>,
    ) = runTransaction(notifyDataChange = true) {
        updatePracticeTitle(title, id)
        charactersToAdd.forEach { insertOrIgnorePracticeEntry(it, id) }
        charactersToRemove.forEach { deletePracticeEntry(id, it) }
    }

    override suspend fun getAllPractices(): List<Practice> = runTransaction {
        getAllPractices().executeAsList().map {
            Practice(it.id, it.name, it.position.toInt())
        }
    }

    override suspend fun getPracticeInfo(
        id: Long,
    ): Practice = runTransaction {
        getPractice(id).executeAsOne().run { Practice(id, name, position.toInt()) }
    }

    override suspend fun getKanjiForPractice(
        id: Long,
    ): List<String> = runTransaction {
        getPracticeEntriesForPractice(id).executeAsList().map { it.character }
    }

    override suspend fun saveWritingReviews(
        practiceTime: Instant,
        reviewResultList: List<CharacterWritingReviewResult>,
    ) = runTransaction(notifyDataChange = true) {
        val mode = practiceTypeToDBValue.getValue(PreferencesLetterPracticeType.Writing).toLong()
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
        reviewResultList: List<CharacterReadingReviewResult>,
    ) = runTransaction(notifyDataChange = true) {
        val mode = practiceTypeToDBValue.getValue(PreferencesLetterPracticeType.Reading).toLong()
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
        type: PreferencesLetterPracticeType,
    ): Instant? = runTransaction {
        val timestamp = when (type) {
            PreferencesLetterPracticeType.Writing -> getFirstWritingReview(character).executeAsOneOrNull()?.timestamp
            PreferencesLetterPracticeType.Reading -> getFirstReadingReview(character).executeAsOneOrNull()?.timestamp
        }
        timestamp?.let { Instant.fromEpochMilliseconds(it) }
    }

    override suspend fun getLastReviewTime(
        practiceId: Long,
        type: PreferencesLetterPracticeType,
    ): Instant? = runTransaction {
        val timestamp = when (type) {
            PreferencesLetterPracticeType.Writing -> getLastWritingReview(practiceId).executeAsOneOrNull()?.timestamp
            PreferencesLetterPracticeType.Reading -> getLastReadingReview(practiceId).executeAsOneOrNull()?.timestamp
        }
        timestamp?.let { Instant.fromEpochMilliseconds(it) }
    }

    override suspend fun getStudyProgress(
        character: String,
        type: PreferencesLetterPracticeType,
    ): CharacterStudyProgress? = runTransaction {
        getCharacterProgress(
            character = character,
            mode = practiceTypeToDBValue.getValue(type)
        ).executeAsOneOrNull()?.converted()
    }

    override suspend fun getStudyProgresses(): List<CharacterStudyProgress> = runTransaction {
        getCharacterProgresses()
            .executeAsList()
            .map { it.converted() }
    }

    override suspend fun getReviews(
        start: Instant,
        end: Instant,
    ): Map<CharacterReviewResult, Instant> = runTransaction {
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

    override suspend fun getTotalReviewsCount(): Long = runTransaction {
        getTotalWritingReviewsCount().executeAsOne() + getTotalReadingReviewsCount().executeAsOne()
    }

    override suspend fun getTotalPracticeTime(
        singleReviewDurationLimit: Long,
    ): Long = runTransaction {
        val writingsDuration = getTotalWritingReviewsDuration(
            reviewDurationLimit = singleReviewDurationLimit
        ).executeAsOne().SUM?.toLong() ?: 0L
        val readingsDuration = getTotalReadingReviewsDuration(
            reviewDurationLimit = singleReviewDurationLimit
        ).executeAsOne().SUM?.toLong() ?: 0L
        writingsDuration + readingsDuration
    }


    override suspend fun getTotalUniqueReviewedCharactersCount(): Long =
        runTransaction { getTotalUniqueReviewedCharactersCount().executeAsOne() }

    private fun CharacterReviewOutcome.toLong(): Long = when (this) {
        CharacterReviewOutcome.Success -> 1
        CharacterReviewOutcome.Fail -> 0
    }

    private fun Character_progress.converted(): CharacterStudyProgress {
        return CharacterStudyProgress(
            character = character,
            practiceType = practiceTypeToDBValue.entries
                .first { mode == it.value }.key,
            lastReviewTime = Instant.fromEpochMilliseconds(last_review_time!!),
            repeats = repeats.toInt(),
            lapses = lapses.toInt()
        )
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
        PreferencesLetterPracticeType.Writing to 0L,
        PreferencesLetterPracticeType.Reading to 1L
    )

}

