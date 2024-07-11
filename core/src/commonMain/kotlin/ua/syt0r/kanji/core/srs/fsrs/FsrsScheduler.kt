package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.datetime.Instant
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
        status = FsrsCardStatus.New,
        params = FsrsCardParams.New,
        interval = Duration.ZERO,
        lapses = 0,
        repeats = 0
    )

    override fun schedule(card: FsrsCard, reviewTime: Instant): FsrsAnswers {
        val again: FsrsCard
        val hard: FsrsCard
        val good: FsrsCard
        val easy: FsrsCard

        when (card.status) {
            FsrsCardStatus.New -> {
                again = card.next(
                    status = FsrsCardStatus.Learning,
                    params = fsrsAlgorithm.updatedParams(
                        card.params,
                        FsrsReviewRating.Again,
                        reviewTime
                    ),
                    interval = 1.minutes
                )
                hard = card.next(
                    status = FsrsCardStatus.Learning,
                    params = fsrsAlgorithm.updatedParams(
                        card.params,
                        FsrsReviewRating.Hard,
                        reviewTime
                    ), interval = 5.minutes
                )
                good = card.next(
                    status = FsrsCardStatus.Learning,
                    params = fsrsAlgorithm.updatedParams(
                        card.params,
                        FsrsReviewRating.Good,
                        reviewTime
                    ), interval = 10.minutes
                )

                val easyParams = fsrsAlgorithm.updatedParams(
                    cardParams = FsrsCardParams.New,
                    rating = FsrsReviewRating.Easy,
                    reviewTime = reviewTime
                )
                easy = card.next(
                    status = FsrsCardStatus.Review,
                    params = easyParams,
                    interval = fsrsAlgorithm.nextInterval(easyParams)
                )
            }

            FsrsCardStatus.Learning,
            FsrsCardStatus.Relearning -> {
                again = card.next(interval = 5.minutes)
                hard = card.next(interval = 10.minutes)

                val goodParams = fsrsAlgorithm.updatedParams(
                    cardParams = card.params,
                    rating = FsrsReviewRating.Good,
                    reviewTime = reviewTime
                )
                val goodInterval = fsrsAlgorithm.nextInterval(goodParams)

                good = card.next(
                    status = FsrsCardStatus.Review,
                    interval = goodInterval
                )

                val easyParams = fsrsAlgorithm.updatedParams(
                    cardParams = card.params,
                    rating = FsrsReviewRating.Easy,
                    reviewTime = reviewTime
                )
                val easyInterval = fsrsAlgorithm.nextInterval(easyParams)

                easy = card.next(
                    status = FsrsCardStatus.Review,
                    interval = listOf(easyInterval, goodInterval + 1.days).max()
                )
            }

            FsrsCardStatus.Review -> {
                val againParams = fsrsAlgorithm
                    .updatedParams(card.params, FsrsReviewRating.Again, reviewTime)

                val hardParams = fsrsAlgorithm
                    .updatedParams(card.params, FsrsReviewRating.Hard, reviewTime)
                var hardInterval = fsrsAlgorithm.nextInterval(hardParams)

                val goodParams = fsrsAlgorithm
                    .updatedParams(card.params, FsrsReviewRating.Good, reviewTime)
                var goodInterval = fsrsAlgorithm.nextInterval(goodParams)

                val easyParams = fsrsAlgorithm
                    .updatedParams(card.params, FsrsReviewRating.Easy, reviewTime)
                var easyInterval = fsrsAlgorithm.nextInterval(easyParams)

                hardInterval = listOf(hardInterval, goodInterval).min()
                goodInterval = listOf(goodInterval, hardInterval + 1.days).max()
                easyInterval = listOf(goodInterval + 1.days, easyInterval).max()

                again = card.next(
                    status = FsrsCardStatus.Relearning,
                    params = againParams,
                    interval = 5.minutes,
                    lapses = card.lapses + 1
                )

                hard = card.next(params = hardParams, interval = hardInterval)
                good = card.next(params = goodParams, interval = goodInterval)
                easy = card.next(params = easyParams, interval = easyInterval)
            }
        }

        return FsrsAnswers(again, hard, good, easy)
    }

}
