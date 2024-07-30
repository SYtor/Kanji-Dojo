package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.srs.use_case.GetSrsStatusUseCase
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.toSrsItemKey

interface VocabSrsManager {
    val dataChangeFlow: SharedFlow<Unit>
    suspend fun getUpdatedDecksData(): SrsDecksData
    suspend fun getUpdatedDeckInfo(deckId: Long): SrsDeckInfo
}

data class SrsDecksData(
    val decks: List<SrsDeckInfo>
)

data class SrsDeckInfo(
    val id: Long,
    val title: String,
    val summaries: Map<VocabPracticeType, VocabDeckSrsProgress>
)

data class VocabDeckSrsProgress(
    val all: List<Long>,
    val done: List<Long>,
    val due: List<Long>,
    val new: List<Long>
)

class DefaultVocabSrsManager(
    private val practiceRepository: VocabPracticeRepository,
    private val srsItemRepository: SrsItemRepository,
    private val getSrsStatusUseCase: GetSrsStatusUseCase,
    coroutineScope: CoroutineScope
) : VocabSrsManager {

    private val _dataChangeFlow = MutableSharedFlow<Unit>()
    override val dataChangeFlow: SharedFlow<Unit> = _dataChangeFlow

    private var cache: SrsDecksData? = null

    init {
        srsItemRepository.updatesFlow
            .onEach {
                cache = null
                _dataChangeFlow.emit(Unit)
            }
            .launchIn(coroutineScope)
    }

    override suspend fun getUpdatedDecksData(): SrsDecksData {
        return getCache()
    }

    override suspend fun getUpdatedDeckInfo(deckId: Long): SrsDeckInfo {
        return getCache().decks.first { it.id == deckId }
    }

    private suspend fun getCache(): SrsDecksData = cache ?: withContext(Dispatchers.IO) {
        val decks = practiceRepository.getDecks()
        val srsCardsMap = srsItemRepository.getAll()

        val srsDeckInfoList = decks.map {
            val deckItems = practiceRepository.getDeckWords(it.id)
            SrsDeckInfo(
                id = it.id,
                title = it.title,
                summaries = VocabPracticeType.values()
                    .associateWith { getVocabDeckSummary(deckItems, srsCardsMap, it) }
            )
        }

        SrsDecksData(srsDeckInfoList)
    }.also { cache = it }

    private fun getVocabDeckSummary(
        items: List<Long>,
        srsCards: Map<SrsCardKey, SrsCard>,
        practiceType: VocabPracticeType
    ): VocabDeckSrsProgress {
        val done = mutableListOf<Long>()
        val due = mutableListOf<Long>()
        val new = mutableListOf<Long>()
        items.forEach { wordId ->
            val key = practiceType.toSrsItemKey(wordId)
            val status = srsCards[key]
                ?.let { it.lastReview?.plus(it.interval) }
                ?.let { getSrsStatusUseCase(it) }
                ?: SrsItemStatus.New
            val list = when (status) {
                SrsItemStatus.New -> new
                SrsItemStatus.Done -> done
                SrsItemStatus.Review -> due
            }
            list.add(wordId)
        }
        return VocabDeckSrsProgress(
            all = items,
            done = done,
            due = due,
            new = new
        )
    }

}