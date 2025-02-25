package ua.syt0r.kanji.core

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.user_data.database.VocabPracticeRepository
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenData

data class ResolvedVocabCard(
    val dictionaryId: Long?,
    val kanjiReading: String?,
    val kanaReading: String,
    val furigana: FuriganaString?,
    val glossary: List<String>,
    val pos: List<String>
)

fun ResolvedVocabCard.toInfoScreenData() =
    InfoScreenData.Vocab(dictionaryId, kanjiReading, kanaReading)

class VocabCardResolver(
    private val vocabPracticeRepository: VocabPracticeRepository,
    private val appDataRepository: AppDataRepository
) {

    private val refreshableCardsFlow = refreshableDataFlow(
        dataChangeFlow = vocabPracticeRepository.changesFlow,
        lifecycleState = MutableStateFlow(LifecycleState.Visible),
        valueProvider = { vocabPracticeRepository.getAllCards().associateBy { it.cardId } }
    )

    suspend fun resolveUserCard(cardId: Long): ResolvedVocabCard {
        val cards = refreshableCardsFlow.await()
        val vocabCard = cards.getValue(cardId)

        return coroutineScope {

            val kanjiReading = vocabCard.data.kanjiReading
            val kanaReading = vocabCard.data.kanaReading

            val word = async(start = CoroutineStart.LAZY) {
                vocabCard.data
                    .run { appDataRepository.findWords(dictionaryId, kanjiReading, kanaReading) }
                    .firstOrNull()
            }

            ResolvedVocabCard(
                dictionaryId = vocabCard.data.dictionaryId,
                kanjiReading = kanjiReading,
                kanaReading = kanaReading,
                furigana = word.await()?.reading?.furigana,
                glossary = vocabCard.data.meaning?.let { listOf(it) }
                    ?: word.await()!!.glossary,
                pos = word.await()?.partOfSpeechList ?: emptyList()
            )
        }
    }

    suspend fun resolveDictionaryCard(
        dictionaryId: Long,
        kanjiReading: String?,
        kanaReading: String
    ): ResolvedVocabCard {
        return coroutineScope {
            val word = appDataRepository.findWords(dictionaryId, kanjiReading, kanaReading)
                .first()

            ResolvedVocabCard(
                dictionaryId = dictionaryId,
                kanjiReading = kanjiReading,
                kanaReading = kanaReading,
                furigana = word.reading.furigana,
                glossary = word.glossary,
                pos = word.partOfSpeechList
            )
        }

    }

    suspend fun <T> Flow<RefreshableData<T>>.await(): T {
        return filterIsInstance<RefreshableData.Loaded<T>>().first().value
    }

}