package ua.syt0r.kanji.core.srs.fsrs

import ua.syt0r.kanji.core.srs.SrsItemKey
import kotlin.time.Duration

interface FsrsItemRepository {
    suspend fun get(key: SrsItemKey): FsrsItemData
    suspend fun update(data: FsrsItemData)
}

class DefaultFsrsItemRepository : FsrsItemRepository {

    private val inMemoryCache = mutableMapOf<SrsItemKey, FsrsItemData>()

    override suspend fun get(key: SrsItemKey): FsrsItemData {
        return inMemoryCache[key] ?: newCard(key)
    }

    override suspend fun update(data: FsrsItemData) {
        inMemoryCache[data.key] = data
    }

    private fun newCard(key: SrsItemKey) = FsrsItemData(
        key = key,
        interval = Duration.ZERO,
        card = FsrsCard.New,
        status = FsrsCardStatus.New,
        lapses = 0,
        repeats = 0
    )

}