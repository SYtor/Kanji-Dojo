package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus.Learning
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus.New
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus.Relearning
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus.Review
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating.Again
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating.Easy
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating.Good
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating.Hard
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

interface FsrsScheduler {
    fun newCard(): FsrsCard
    fun schedule(card: FsrsCard, reviewTime: Instant): FsrsAnswers
}

class DefaultFsrsScheduler(
    private val fsrsAlgorithm: FsrsAlgorithm,
) : FsrsScheduler {

    override fun newCard() = FsrsCard(
        status = New,
        params = FsrsCardParams.New,
        interval = Duration.ZERO,
        lapses = 0,
        repeats = 0
    )

    override fun schedule(card: FsrsCard, reviewTime: Instant): FsrsAnswers {

        fun FsrsCard.nextCard(
            status: FsrsCardStatus,
            rating: FsrsReviewRating,
            overriddenInterval: Duration? = null,
            incrementLapses: Boolean = false
        ): FsrsCard {
            val updatedParams = fsrsAlgorithm.updatedParams(this, rating, reviewTime)
            return FsrsCard(
                status = status,
                params = updatedParams,
                interval = overriddenInterval ?: fsrsAlgorithm.nextInterval(updatedParams),
                lapses = if (incrementLapses) lapses + 1 else lapses,
                repeats = repeats + 1
            )
        }

        val again: FsrsCard
        val hard: FsrsCard
        val good: FsrsCard
        val easy: FsrsCard

        when (card.status) {
            New -> {
                again = card.nextCard(Learning, Again, 1.minutes)
                hard = card.nextCard(Learning, Hard, 5.minutes)
                good = card.nextCard(Learning, Good, 10.minutes)
                easy = card.nextCard(Review, Easy)
            }

            Learning,
            Relearning -> {
                again = card.nextCard(card.status, Again, 5.minutes)
                hard = card.nextCard(card.status, Hard, 10.minutes)
                good = card.nextCard(Review, Good)

                val tmpEasy = card.nextCard(Review, Easy)
                val easyInterval = maxOf(tmpEasy.interval, good.interval + 1.days)

                easy = tmpEasy.copy(interval = easyInterval)
            }

            Review -> {
                again = card.nextCard(Relearning, Again, 5.minutes, true)

                val tmpHard = card.nextCard(Review, Hard, incrementLapses = true)
                val tmpGood = card.nextCard(Review, Good, incrementLapses = true)
                val tmpEasy = card.nextCard(Review, Easy, incrementLapses = true)

                val hardInterval = minOf(tmpHard.interval, tmpGood.interval)
                val goodInterval = maxOf(tmpGood.interval, hardInterval + 1.days)
                val easyInterval = maxOf(tmpEasy.interval, goodInterval + 1.days)

                hard = tmpHard.copy(interval = hardInterval)
                good = tmpGood.copy(interval = goodInterval)
                easy = tmpEasy.copy(interval = easyInterval)
            }
        }

        return FsrsAnswers(again, hard, good, easy)
    }

}
