package ua.syt0r.kanji.core.srs.fsrs

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import ua.syt0r.kanji.core.srs.SrsCardKey


class FsrsItemRepository {

    private val _updatesFlow = MutableSharedFlow<Unit>()
    val updatesFlow: SharedFlow<Unit> = _updatesFlow

    private val inMemoryCache = mutableMapOf<SrsCardKey, FsrsCard>()

    suspend fun get(key: SrsCardKey): FsrsCard? {
        return inMemoryCache[key]
    }

    suspend fun update(key: SrsCardKey, card: FsrsCard) {
        inMemoryCache[key] = card
        _updatesFlow.emit(Unit)
    }

}