package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.SrsAnswer
import ua.syt0r.kanji.core.srs.SrsItemData
import ua.syt0r.kanji.core.srs.SrsItemKey
import kotlin.time.Duration


data class FsrsItemData(
    override val key: SrsItemKey,
    override val interval: Duration,
    val card: FsrsCard,
    val status: FsrsCardStatus,
    val lapses: Int,
    val repeats: Int
) : SrsItemData

enum class FsrsCardStatus {
    New, Learning, Review, Relearning
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
) : SrsAnswer {
    Again(1), Hard(2), Good(3), Easy(4)
}
