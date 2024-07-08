package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.core.srs.fsrs.FsrsItemRepository
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.DashboardVocabDeck
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDeckSrsProgress
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.vocabDecks
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.toSrsItemKey

interface SubscribeOnDashboardVocabDecksUseCase {
    operator fun invoke(
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<VocabDecks>>
}

data class VocabDecks(
    val userDecks: List<DashboardVocabDeck>,
    val defaultDecks: List<DashboardVocabDeck>
)

class DefaultSubscribeOnDashboardVocabDecksUseCase(
    private val repository: VocabPracticeRepository,
    private val srsItemRepository: FsrsItemRepository,
    private val timeUtils: TimeUtils
) : SubscribeOnDashboardVocabDecksUseCase {

    override fun invoke(
        lifecycleState: StateFlow<LifecycleState>
    ): Flow<RefreshableData<VocabDecks>> {
        return refreshableDataFlow(
            dataChangeFlow = repository.changesFlow,
            lifecycleState = lifecycleState,
            valueProvider = { getUpdatedDecks() }
        )
    }

    private suspend fun getUpdatedDecks(): VocabDecks {
        Logger.logMethod()

        val userDecks = repository.getDecks()

        val deckWords = userDecks.flatMap { repository.getDeckWords(it.id) }.distinct()
        val instant = timeUtils.now()

        val wordProgresses: Map<Long, WordSrsProgress> = deckWords.associateWith { wordId ->
            WordSrsProgress(
                statuses = VocabPracticeType.values().associateWith { practiceType ->
                    val srsItemData = srsItemRepository.get(practiceType.toSrsItemKey(wordId))

                    when {
                        srsItemData.lastReview == null -> SrsItemStatus.New
                        srsItemData.lastReview + srsItemData.interval > instant -> SrsItemStatus.Done
                        else -> SrsItemStatus.Review
                    }
                }
            )
        }

        return VocabDecks(
            userDecks = userDecks.map {
                val words = repository.getDeckWords(it.id)
                DashboardVocabDeck(
                    titleResolver = { it.title },
                    expressionIds = words,
                    srsProgress = getVocabDeckSrsProgress(words, wordProgresses),
                    id = it.id
                )
            },
            defaultDecks = vocabDecks.map {
                DashboardVocabDeck(
                    titleResolver = it.titleResolver,
                    expressionIds = it.expressionIds,
                    srsProgress = getVocabDeckSrsProgress(it.expressionIds, wordProgresses)
                )
            }
        )
    }

    private fun getVocabDeckSrsProgress(
        words: List<Long>,
        cache: Map<Long, WordSrsProgress>
    ): Map<VocabPracticeType, VocabDeckSrsProgress> {
        return VocabPracticeType.values().associateWith { vocabPracticeType ->
            val done = mutableListOf<Long>()
            val due = mutableListOf<Long>()
            val new = mutableListOf<Long>()
            words.forEach { word ->
                val status = cache[word]?.statuses?.get(vocabPracticeType) ?: SrsItemStatus.New
                val list = when (status) {
                    SrsItemStatus.New -> new
                    SrsItemStatus.Done -> done
                    SrsItemStatus.Review -> due
                }
                list.add(word)
            }
            VocabDeckSrsProgress(
                all = words,
                done = done,
                due = due,
                new = new
            )
        }
    }

    data class WordSrsProgress(
        val statuses: Map<VocabPracticeType, SrsItemStatus>
    )

}