package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.datetime.Instant
import kotlin.math.exp
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

interface FSRS {
    fun getUpdatedCard(card: FsrsCard, answer: FsrsAnswer, reviewTime: Instant): FsrsCard.Existing
    fun getInterval(card: FsrsCard.Existing, now: Instant): Duration
}

sealed interface FsrsCard {

    object New : FsrsCard

    data class Existing(
        val difficulty: Double,
        val stability: Double,
        val reviewTime: Instant
    ) : FsrsCard

}

enum class FsrsAnswer(
    val grade: Int
) {
    Again(1), Hard(2), Good(3), Easy(4)
}

class DefaultFSRS(
    private val w: Array<Double> = DefaultWeights
) : FSRS {

    private val decay = -0.5
    private val factor = 19.0 / 81.0

    override fun getUpdatedCard(
        card: FsrsCard,
        answer: FsrsAnswer,
        reviewTime: Instant
    ): FsrsCard.Existing {
        return when (card) {
            FsrsCard.New -> FsrsCard.Existing(
                difficulty = initialDifficulty(answer.grade),
                stability = initialStability(answer.grade),
                reviewTime = reviewTime
            )

            is FsrsCard.Existing -> {
                val difficulty = difficulty(
                    difficulty = card.difficulty,
                    grade = answer.grade
                )

                val retrievability = retrievability(
                    elapsedDuration = reviewTime - card.reviewTime,
                    stability = card.stability
                )

                val stability = when (answer) {
                    FsrsAnswer.Again -> forgetStability(
                        difficulty = card.difficulty,
                        stability = card.stability,
                        retrievability = retrievability
                    )

                    else -> successStability(
                        difficulty = card.difficulty,
                        stability = card.stability,
                        retrievability = retrievability,
                        grade = answer.grade
                    )
                }

                FsrsCard.Existing(
                    difficulty = difficulty,
                    stability = stability,
                    reviewTime = reviewTime
                )
            }
        }
    }

    override fun getInterval(card: FsrsCard.Existing, now: Instant): Duration {
        val retrievability = retrievability(now - card.reviewTime, card.stability)
        val interval = card.stability / factor * (retrievability.pow(1 / decay) - 1)
        return interval.days
    }

    private fun initialStability(grade: Int): Double {
        return w[grade - 1]
    }

    private fun initialDifficulty(grade: Int): Double {
        return w[4] - (grade - 3) * w[5]
    }

    private fun difficulty(difficulty: Double, grade: Int): Double {
        return w[7] * initialDifficulty(3) + (1 - w[7]) * (difficulty - w[6] * (grade - 3))
    }

    private fun retrievability(elapsedDuration: Duration, stability: Double): Double {
        val days = elapsedDuration.toDouble(DurationUnit.DAYS)
        return (1 + factor * days / stability).pow(decay)
    }

    private fun successStability(
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
                ((stability + 1).pow(w[13]) - 1) * exp(w[14] * (1 - retrievability))
    }

}

private val DefaultWeights = arrayOf(
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
