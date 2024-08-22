package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating
import ua.syt0r.kanji.core.srs.fsrs.FsrsScheduler
import ua.syt0r.kanji.core.user_data.practice.FsrsItemRepository
import kotlin.time.Duration

sealed interface PracticeType

interface PracticeTypeItem {
    val srsPracticeType: SrsPracticeType
}

enum class LetterPracticeType(
    override val srsPracticeType: SrsPracticeType
) : PracticeType, PracticeTypeItem {

    Writing(SrsPracticeType.LetterWriting),
    Reading(SrsPracticeType.LetterReading);

    fun toSrsKey(letter: String) = SrsCardKey(letter, srsPracticeType.value)

    companion object {
        val srsPracticeTypeValues: List<Long> = values().map { it.srsPracticeType.value }
    }

}

enum class VocabPracticeType(
    override val srsPracticeType: SrsPracticeType
) : PracticeType, PracticeTypeItem {

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

interface SrsItemRepository {

    val updatesFlow: SharedFlow<Unit>

    suspend fun get(key: SrsCardKey): SrsCard?
    suspend fun getAll(): Map<SrsCardKey, SrsCard>
    suspend fun update(key: SrsCardKey, card: SrsCard)

}

interface SrsScheduler {
    fun newCard(): SrsCard
    fun answers(data: SrsCard, reviewTime: Instant): SrsAnswers
}

class DefaultSrsItemRepository(
    private val fsrsItemRepository: FsrsItemRepository
) : SrsItemRepository {

    override val updatesFlow: SharedFlow<Unit> = fsrsItemRepository.updatesFlow

    override suspend fun get(key: SrsCardKey): SrsCard? {
        return fsrsItemRepository.get(key)?.let { SrsCard(it) }
    }

    override suspend fun getAll(): Map<SrsCardKey, SrsCard> {
        return fsrsItemRepository.getAll().mapValues { SrsCard(it.value) }
    }

    override suspend fun update(key: SrsCardKey, card: SrsCard) {
        fsrsItemRepository.update(key, card.fsrsCard)
    }

}

class DefaultSrsScheduler(
    private val fsrsScheduler: FsrsScheduler
) : SrsScheduler {

    override fun newCard(): SrsCard = SrsCard(fsrsScheduler.newCard())

    override fun answers(
        data: SrsCard,
        reviewTime: Instant
    ): SrsAnswers {
        return fsrsScheduler.schedule(data.fsrsCard, reviewTime).let {
            SrsAnswers(
                again = SrsAnswer(FsrsReviewRating.Again.grade, SrsCard(it.again)),
                hard = SrsAnswer(FsrsReviewRating.Hard.grade, SrsCard(it.hard)),
                good = SrsAnswer(FsrsReviewRating.Good.grade, SrsCard(it.good)),
                easy = SrsAnswer(FsrsReviewRating.Easy.grade, SrsCard(it.easy))
            )
        }
    }

}
