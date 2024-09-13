package ua.syt0r.kanji.core.srs

import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import kotlin.time.Duration

sealed interface PracticeType {
    val srsPracticeType: SrsPracticeType
}

enum class LetterPracticeType(
    override val srsPracticeType: SrsPracticeType
) : PracticeType {

    Writing(SrsPracticeType.LetterWriting),
    Reading(SrsPracticeType.LetterReading);

    fun toSrsKey(letter: String) = SrsCardKey(letter, srsPracticeType.value)

    companion object {
        val srsPracticeTypeValues: List<Long> = values().map { it.srsPracticeType.value }
    }

}

enum class VocabPracticeType(
    override val srsPracticeType: SrsPracticeType
) : PracticeType {

    Flashcard(SrsPracticeType.VocabFlashcard),
    ReadingPicker(SrsPracticeType.VocabReadingPicker),
    Writing(SrsPracticeType.VocabWriting);

    fun toSrsKey(wordId: Long) = SrsCardKey(wordId.toString(), srsPracticeType.value)

    companion object {
        val srsPracticeTypeValues: List<Long> = values().map { it.srsPracticeType.value }
    }

}

data class SrsCardKey(
    val itemKey: String,
    val practiceType: Long
)

enum class SrsPracticeType(val value: Long) {

    LetterWriting(0),
    LetterReading(1),

    VocabFlashcard(10),
    VocabReadingPicker(11),
    VocabWriting(12);

}

data class SrsCard(
    val fsrsCard: FsrsCard
) {
    val lastReview: Instant? = fsrsCard.lastReview
    val interval: Duration = fsrsCard.interval
    val expectedReview = lastReview?.plus(interval)
}

enum class SrsItemStatus { New, Done, Review }

data class SrsAnswer(
    val grade: Int,
    val card: SrsCard
)

data class SrsAnswers(
    val again: SrsAnswer,
    val hard: SrsAnswer,
    val good: SrsAnswer,
    val easy: SrsAnswer
)
