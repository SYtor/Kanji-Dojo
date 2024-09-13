package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.flow.SharedFlow
import ua.syt0r.kanji.core.user_data.practice.FsrsItemRepository

interface SrsItemRepository {

    val updatesFlow: SharedFlow<Unit>

    suspend fun get(key: SrsCardKey): SrsCard?
    suspend fun getAll(): Map<SrsCardKey, SrsCard>
    suspend fun update(key: SrsCardKey, card: SrsCard)

}

class DefaultSrsItemRepository(
    private val fsrsItemRepository: FsrsItemRepository
) : SrsItemRepository {

    override val updatesFlow: SharedFlow<Unit> = fsrsItemRepository.updatesFlow

    override suspend fun get(key: SrsCardKey): SrsCard? {
        return fsrsItemRepository.get(key)?.let { SrsCard(it) }
    }

    override suspend fun getAll(): Map<SrsCardKey, SrsCard> {
        return fsrsItemRepository.getAll().mapValues { SrsCard(it.value) }
    }

    override suspend fun update(key: SrsCardKey, card: SrsCard) {
        fsrsItemRepository.update(key, card.fsrsCard)
    }

}