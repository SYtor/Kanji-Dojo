package ua.syt0r.kanji.core.srs

import kotlinx.datetime.Instant
import kotlin.time.Duration

data class SrsItemKey(
    val itemKey: String,
    val practiceType: String
)

interface SrsItemData {
    val key: SrsItemKey
    val lastReview: Instant?
    val interval: Duration
}

enum class SrsItemStatus { New, Done, Review }

interface SrsAnswer

interface SrsScheduler {

    fun get(
        data: SrsItemData,
        answer: SrsAnswer,
        reviewTime: Instant
    ): SrsItemData

}
