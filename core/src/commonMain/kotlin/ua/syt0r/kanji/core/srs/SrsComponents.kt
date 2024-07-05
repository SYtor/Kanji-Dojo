package ua.syt0r.kanji.core.srs

import kotlinx.datetime.Instant
import kotlin.time.Duration

data class SrsItemKey(
    val itemKey: String,
    val practiceType: String
)

interface SrsItemData {
    val key: SrsItemKey
    val interval: Duration
}

interface SrsAnswer

interface SrsScheduler {

    fun get(
        data: SrsItemData,
        answer: SrsAnswer,
        reviewTime: Instant
    ): SrsItemData

}
