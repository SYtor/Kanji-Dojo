package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.datetime.Instant
import kotlin.math.exp
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

interface FsrsAlgorithm {

    fun updatedParams(
        cardParams: FsrsCardParams,
        rating: FsrsReviewRating,
        reviewTime: Instant
    ): FsrsCardParams.Existing

    fun nextInterval(
        cardParams: FsrsCardParams.Existing
    ): Duration

}

abstract class Fsrs(
    private val w: Array<Double>
) : FsrsAlgorithm {

    override fun updatedParams(
        cardParams: FsrsCardParams,
        rating: FsrsReviewRating,
        reviewTime: Instant
    ): FsrsCardParams.Existing {
        return when (cardParams) {
            FsrsCardParams.New -> FsrsCardParams.Existing(
                difficulty = initialDifficulty(rating.grade),
                stability = initialStability(rating.grade),
                reviewTime = reviewTime
            )

            is FsrsCardParams.Existing -> {
                val difficulty = difficulty(
                    difficulty = cardParams.difficulty,
                    grade = rating.grade
                )

                val retrievability = retrievability(
                    elapsedDuration = reviewTime - cardParams.reviewTime,
                    stability = cardParams.stability
                )

                val stability = when (rating) {
                    FsrsReviewRating.Again -> forgetStability(
                        difficulty = cardParams.difficulty,
                        stability = cardParams.stability,
                        retrievability = retrievability
                    )

                    else -> recallStability(
                        difficulty = cardParams.difficulty,
                        stability = cardParams.stability,
                        retrievability = retrievability,
                        grade = rating.grade
                    )
                }

                FsrsCardParams.Existing(
                    difficulty = difficulty,
                    stability = stability,
                    reviewTime = reviewTime
                )
            }
        }
    }

    protected abstract fun retrievability(elapsedDuration: Duration, stability: Double): Double

    private fun initialStability(grade: Int): Double {
        return w[grade - 1]
    }

    private fun initialDifficulty(grade: Int): Double {
        val value = w[4] - (grade - 3) * w[5]
        return value.coerceIn(1.0, 10.0)
    }

    private fun difficulty(difficulty: Double, grade: Int): Double {
        val value = w[7] * initialDifficulty(3) + (1 - w[7]) * (difficulty - w[6] * (grade - 3))
        return value.coerceIn(1.0, 10.0)
    }

    private fun recallStability(
        difficulty: Double,
        stability: Double,
        retrievability: Double,
        grade: Int
    ): Double {
        val gradeMultiplier: Double = when (grade) {
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

}

class Fsrs4(
    w: Array<Double> = WEIGHTS,
    private val decay: Double = DECAY,
    private val factor: Double = FACTOR,
    private val requestRetention: Double = REQUEST_RETENTION
) : Fsrs(w) {

    override fun retrievability(elapsedDuration: Duration, stability: Double): Double {
        val days = elapsedDuration.toDouble(DurationUnit.DAYS)
        return (1 + factor * days / stability).pow(decay)
    }

    override fun nextInterval(cardParams: FsrsCardParams.Existing): Duration {
        return (9 * cardParams.stability * (1 / requestRetention - 1)).days
    }

    companion object {
        val WEIGHTS = arrayOf(
            0.4,
            0.6,
            2.4,
            5.8,
            4.93,
            0.94,
            0.86,
            0.01,
            1.49,
            0.14,
            0.94,
            2.18,
            0.05,
            0.34,
            1.26,
            0.29,
            2.61
        )
        const val DECAY = -1.0
        const val FACTOR = 1.0 / 9
        const val REQUEST_RETENTION = 0.9
    }

}

class Fsrs45(
    w: Array<Double> = WEIGHTS,
    private val decay: Double = DECAY,
    private val factor: Double = FACTOR,
    private val requestRetention: Double = REQUEST_RETENTION
) : Fsrs(w) {

    override fun retrievability(elapsedDuration: Duration, stability: Double): Double {
        val days = elapsedDuration.inWholeDays.toDouble()
        return (1.0 + factor * days / stability).pow(decay)
    }

    override fun nextInterval(cardParams: FsrsCardParams.Existing): Duration {
        val retrievability = requestRetention
        val interval = cardParams.stability / factor * (retrievability.pow(1 / decay) - 1)
        return interval.days
    }

    companion object {
        val WEIGHTS = arrayOf(
            0.4872,
            1.4003,
            3.7145,
            13.8206,
            5.1618,
            1.2298,
            0.8975,
            0.031,
            1.6474,
            0.1367,
            1.0461,
            2.1072,
            0.0793,
            0.3246,
            1.587,
            0.2272,
            2.8755
        )
        const val DECAY = -0.5
        const val FACTOR = 19.0 / 81.0
        const val REQUEST_RETENTION = 0.9
    }

}
