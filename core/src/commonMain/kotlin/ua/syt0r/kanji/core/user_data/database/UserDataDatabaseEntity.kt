package ua.syt0r.kanji.core.user_data.database

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.core.srs.LetterPracticeType
import java.io.File
import kotlin.time.Duration


class UserDatabaseInfo(
    val version: Long,
    val file: File
)


data class LetterDeck(
    val id: Long,
    val name: String,
    val position: Int,
)

data class CharacterStudyProgress(
    val character: String,
    val practiceType: LetterPracticeType,
    val lastReviewTime: Instant,
    val repeats: Int,
    val lapses: Int,
)


data class VocabDeck(
    val id: Long,
    val title: String,
    val position: Int
)

data class VocabCardData(
    val kanjiReading: String?,
    val kanaReading: String,
    val meaning: String?,
    val dictionaryId: Long
)

data class SavedVocabCard(
    val cardId: Long,
    val deckId: Long,
    val data: VocabCardData
)


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