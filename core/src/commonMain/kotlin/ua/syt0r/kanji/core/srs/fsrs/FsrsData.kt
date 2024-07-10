package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.datetime.Instant
import kotlin.time.Duration

data class FsrsCard(
    val status: FsrsCardStatus,
    val params: FsrsCardParams,
    val interval: Duration,
    val lapses: Int,
    val repeats: Int
) {

    val lastReview: Instant? = when (params) {
        FsrsCardParams.New -> null
        is FsrsCardParams.Existing -> params.reviewTime
    }

    fun next(
        status: FsrsCardStatus = this.status,
        params: FsrsCardParams = this.params,
        interval: Duration = this.interval,
        lapses: Int = this.lapses
    ): FsrsCard = copy(
        status = status,
        params = params,
        interval = interval,
        lapses = lapses,
        repeats = repeats + 1
    )

}

sealed interface FsrsCardParams {

    object New : FsrsCardParams

    data class Existing(
        val difficulty: Double,
        val stability: Double,
        val reviewTime: Instant
    ) : FsrsCardParams

}

enum class FsrsCardStatus {
    New, Learning, Review, Relearning
}

enum class FsrsReviewRating(val grade: Int) {
    Again(1), Hard(2), Good(3), Easy(4)
}

data class FsrsAnswers(
    val again: FsrsCard,
    val hard: FsrsCard,
    val good: FsrsCard,
    val easy: FsrsCard
)
