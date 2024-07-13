package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.user_data.practice.FsrsItemRepository
import ua.syt0r.kanji.core.srs.fsrs.FsrsScheduler
import kotlin.time.Duration

data class SrsCardKey(
    val itemKey: String,
    val practiceType: String
)

data class SrsCard(
    val fsrsCard: FsrsCard
) {
    val lastReview: Instant? = fsrsCard.lastReview
    val interval: Duration = fsrsCard.interval
}

enum class SrsItemStatus { New, Done, Review }

data class SrsAnswer(
    val again: SrsCard,
    val hard: SrsCard,
    val good: SrsCard,
    val easy: SrsCard
)

interface SrsItemRepository {

    val updatesFlow: SharedFlow<Unit>

    suspend fun get(key: SrsCardKey): SrsCard?
    suspend fun update(key: SrsCardKey, card: SrsCard)

}

interface SrsScheduler {
    fun newCard(): SrsCard
    fun answers(data: SrsCard, reviewTime: Instant): SrsAnswer
}

class DefaultSrsItemRepository(
    private val fsrsItemRepository: FsrsItemRepository
) : SrsItemRepository {

    override val updatesFlow: SharedFlow<Unit> = fsrsItemRepository.updatesFlow

    override suspend fun get(key: SrsCardKey): SrsCard? {
        return fsrsItemRepository.get(key)?.let { SrsCard(it) }
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
    ): SrsAnswer {
        return fsrsScheduler.schedule(data.fsrsCard, reviewTime).let {
            SrsAnswer(
                again = SrsCard(it.again),
                hard = SrsCard(it.hard),
                good = SrsCard(it.good),
                easy = SrsCard(it.easy)
            )
        }
    }

}
