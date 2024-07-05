package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.SrsAnswer
import ua.syt0r.kanji.core.srs.SrsItemData
import ua.syt0r.kanji.core.srs.SrsScheduler
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes


class FsrsScheduler(
    private val fsrsAlgorithm: FsrsAlgorithm,
) : SrsScheduler {

    override fun get(
        data: SrsItemData,
        answer: SrsAnswer,
        reviewTime: Instant
    ): SrsItemData {
        data as FsrsItemData
        answer as FsrsAnswer

        return data.run {
            val currentCard = card
            val nextCard = when (status) {
                FsrsCardStatus.New, FsrsCardStatus.Review -> {
                    fsrsAlgorithm.updatedCard(card, answer, reviewTime)
                }

                else -> currentCard
            }

            val (newStatus, newInterval) = getUpdatedStatusAndInterval(
                currentCard = currentCard,
                nextCard = nextCard as FsrsCard.Existing,
                cardStatus = status,
                answer = answer,
                reviewTime = reviewTime
            )

            copy(
                lapses = if (answer == FsrsAnswer.Again) lapses + 1 else lapses,
                status = newStatus,
                repeats = repeats + 1,
                card = nextCard,
                interval = newInterval
            )
        }
    }

    private fun getUpdatedStatusAndInterval(
        currentCard: FsrsCard,
        nextCard: FsrsCard.Existing,
        cardStatus: FsrsCardStatus,
        answer: FsrsAnswer,
        reviewTime: Instant
    ): Pair<FsrsCardStatus, Duration> = when (cardStatus) {
        FsrsCardStatus.New -> when (answer) {
            FsrsAnswer.Again -> FsrsCardStatus.Learning to 1.minutes
            FsrsAnswer.Hard -> FsrsCardStatus.Learning to 5.minutes
            FsrsAnswer.Good -> FsrsCardStatus.Learning to 10.minutes
            FsrsAnswer.Easy -> FsrsCardStatus.Review to fsrsAlgorithm.nextInterval(nextCard)
        }

        FsrsCardStatus.Learning,
        FsrsCardStatus.Relearning -> when (answer) {
            FsrsAnswer.Again -> cardStatus to 5.minutes
            FsrsAnswer.Hard -> cardStatus to 10.minutes

            FsrsAnswer.Good -> FsrsCardStatus.Review to fsrsAlgorithm.nextInterval(nextCard)
            FsrsAnswer.Easy -> {
                val goodInterval = fsrsAlgorithm.nextInterval(
                    fsrsAlgorithm.updatedCard(currentCard, FsrsAnswer.Good, reviewTime)
                )
                val easyInterval = fsrsAlgorithm.nextInterval(nextCard)
                val interval = listOf(easyInterval, goodInterval + 1.days).max()

                FsrsCardStatus.Review to interval
            }
        }

        FsrsCardStatus.Review -> {
            val hardInterval = fsrsAlgorithm.nextInterval(
                fsrsAlgorithm.updatedCard(currentCard, FsrsAnswer.Hard, reviewTime)
            )
            val goodInterval = fsrsAlgorithm.nextInterval(
                fsrsAlgorithm.updatedCard(currentCard, FsrsAnswer.Good, reviewTime)
            )
            val easyInterval = fsrsAlgorithm.nextInterval(
                fsrsAlgorithm.updatedCard(currentCard, FsrsAnswer.Easy, reviewTime)
            )
            when (answer) {
                FsrsAnswer.Again -> FsrsCardStatus.Relearning to Duration.ZERO
                FsrsAnswer.Hard -> {
                    val interval = listOf(hardInterval, goodInterval).min()
                    FsrsCardStatus.Review to interval
                }

                FsrsAnswer.Good -> {
                    val interval = listOf(hardInterval + 1.days, goodInterval).max()
                    FsrsCardStatus.Review to interval
                }

                FsrsAnswer.Easy -> {
                    val interval = listOf(goodInterval + 1.days, easyInterval).max()
                    FsrsCardStatus.Review to interval
                }
            }
        }
    }

}