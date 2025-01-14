package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating.Easy
import kotlin.math.exp
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

interface FsrsAlgorithm {

    fun updatedParams(
        card: FsrsCard,
        rating: FsrsReviewRating,
        reviewTime: Instant
    ): FsrsCardParams.Existing

    fun nextInterval(
        cardParams: FsrsCardParams.Existing
    ): Duration

}

data class FsrsAlgorithmConfiguration(
    val w: List<Double>,
    val factor: Double,
    val decay: Double,
    val requestRetention: Double,
    val maxInterval: Duration
)

val fsrs5Configuration = FsrsAlgorithmConfiguration(
    w = listOf(
        0.4072, 1.1829, 3.1262, 15.4722, 7.2102, 0.5316, 1.0651, 0.0234, 1.616, 0.1544, 1.0824,
        1.9813, 0.0953, 0.2975, 2.2042, 0.2407, 2.9466, 0.5034, 0.6567
    ),
    factor = 19.0 / 81,
    decay = -0.5,
    requestRetention = 0.9,
    maxInterval = 355.days
)

class Fsrs5(
    configuration: FsrsAlgorithmConfiguration = fsrs5Configuration
) : FsrsAlgorithm {

    private val w: List<Double> = configuration.w
    private val factor: Double = configuration.factor
    private val decay: Double = configuration.decay
    private val requestRetention = configuration.requestRetention
    private val maxInterval = configuration.maxInterval
    private val minimalStability = 0.01

    override fun updatedParams(
        card: FsrsCard,
        rating: FsrsReviewRating,
        reviewTime: Instant
    ): FsrsCardParams.Existing {
        return when (card.params) {
            FsrsCardParams.New -> FsrsCardParams.Existing(
                difficulty = initialDifficulty(rating),
                stability = initialStability(rating),
                reviewTime = reviewTime
            )

            is FsrsCardParams.Existing -> {
                val difficulty = nextDifficulty(
                    difficulty = card.params.difficulty,
                    rating = rating
                )

                val retrievability = when (card.status) {
                    FsrsCardStatus.Learning, FsrsCardStatus.Relearning -> {
                        forgettingCurve(
                            elapsedDuration = card.interval,
                            stability = card.params.stability
                        )
                    }

                    else -> {
                        forgettingCurve(
                            elapsedDuration = reviewTime - card.params.reviewTime,
                            stability = card.params.stability
                        )
                    }
                }

                val stability = nextStability(card.params, card.status, rating, retrievability)
                    .coerceAtLeast(minimalStability)

                FsrsCardParams.Existing(
                    difficulty = difficulty,
                    stability = stability,
                    reviewTime = reviewTime
                )
            }
        }
    }

    override fun nextInterval(cardParams: FsrsCardParams.Existing): Duration {
        val interval = (9 * cardParams.stability * (1 / requestRetention - 1))
        return if (interval.isNaN()) maxInterval else interval.days.coerceAtMost(maxInterval)
    }


    private fun initialDifficulty(rating: FsrsReviewRating): Double {
        return (w[4] - exp(w[5] * (rating.grade - 1)) + 1).coerceIn(1.0, 10.0)
    }

    private fun initialStability(rating: FsrsReviewRating): Double {
        return w[rating.grade - 1]
    }

    private fun forgettingCurve(elapsedDuration: Duration, stability: Double): Double {
        val elapsedDays = elapsedDuration.toDouble(DurationUnit.DAYS)
        return (1 + factor * elapsedDays / stability).pow(decay)
    }

    private fun nextDifficulty(difficulty: Double, rating: FsrsReviewRating): Double {
        val nextDifficulty = difficulty - w[6] * (rating.grade - 3)
        val meanReversion = w[7] * initialDifficulty(Easy) + (1 - w[7]) * nextDifficulty
        return meanReversion.coerceIn(1.0, 10.0)
    }

    private fun nextStability(
        params: FsrsCardParams.Existing,
        status: FsrsCardStatus,
        rating: FsrsReviewRating,
        retrievability: Double
    ): Double = params.run {
        when (status) {
            FsrsCardStatus.New -> error("only existing params supported")
            FsrsCardStatus.Learning, FsrsCardStatus.Relearning -> {
                shortTermStability(stability, rating)
            }

            FsrsCardStatus.Review -> {
                when (rating) {
                    FsrsReviewRating.Again -> {
                        forgetStability(difficulty, stability, retrievability)
                    }

                    else -> {
                        recallStability(difficulty, stability, retrievability, rating)
                    }
                }
            }
        }
    }

    private fun shortTermStability(stability: Double, rating: FsrsReviewRating): Double {
        return stability * exp(w[17] * (rating.grade - 3 + w[18]))
    }

    private fun forgetStability(
        difficulty: Double,
        stability: Double,
        retrievability: Double
    ): Double {
        return w[11] *
                difficulty.pow(-w[12]) *
                ((stability + 1).pow(w[13]) - 1) *
                exp(w[14] * (1 - retrievability))
    }

    private fun recallStability(
        difficulty: Double,
        stability: Double,
        retrievability: Double,
        rating: FsrsReviewRating
    ): Double {
        val gradeMultiplier: Double = when (rating.grade) {
            2 -> w[15]
            4 -> w[16]
            else -> 1.0
        }
        return stability * (
                exp(w[8]) *
                        (11 - difficulty) *
                        stability.pow(-w[9]) *
                        (exp(w[10] * (1 - retrievability)) - 1) *
                        gradeMultiplier + 1
                )
    }

}
