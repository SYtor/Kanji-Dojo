package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import ua.syt0r.kanji.core.srs.SrsItemKey
import kotlin.time.Duration

interface FsrsItemRepository {

    val updatesFlow: SharedFlow<Unit>

    suspend fun get(key: SrsItemKey): FsrsItemData
    suspend fun update(data: FsrsItemData)

}

class DefaultFsrsItemRepository : FsrsItemRepository {

    private val _updatesFlow = MutableSharedFlow<Unit>()
    override val updatesFlow: SharedFlow<Unit> = _updatesFlow

    private val inMemoryCache = mutableMapOf<SrsItemKey, FsrsItemData>()

    override suspend fun get(key: SrsItemKey): FsrsItemData {
        return inMemoryCache[key] ?: newCard(key)
    }

    override suspend fun update(data: FsrsItemData) {
        inMemoryCache[data.key] = data
        _updatesFlow.emit(Unit)
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