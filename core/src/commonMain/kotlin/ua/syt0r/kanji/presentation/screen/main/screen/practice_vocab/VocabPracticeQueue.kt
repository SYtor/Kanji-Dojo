package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.debounceFirst
import ua.syt0r.kanji.core.srs.SrsCard
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.SrsItemRepository
import ua.syt0r.kanji.core.srs.SrsScheduler
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeSrsAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabQueueProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.toSrsItemKey
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeFlashcardDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeReadingDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeSummaryItemUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeWritingDataUseCase
import kotlin.math.min
import kotlin.time.Duration.Companion.days


interface VocabPracticeQueue {
    val state: StateFlow<VocabReviewQueueState>

    suspend fun initialize(expressions: List<VocabQueueItemDescriptor>)
    suspend fun completeCurrentReview(srsItem: SrsCard)
    fun finishPractice()
}

data class VocabPracticeQueueItem(
    val descriptor: VocabQueueItemDescriptor,
    val srsCardKey: SrsCardKey,
    val srsCard: SrsCard,
    val repeats: Int,
    val deferredState: Deferred<VocabPracticeItemData>
)

class DefaultVocabPracticeQueue(
    private val coroutineScope: CoroutineScope,
    private val timeUtils: TimeUtils,
    private val srsItemRepository: SrsItemRepository,
    private val srsScheduler: SrsScheduler,
    private val getFlashcardReviewStateUseCase: GetVocabPracticeFlashcardDataUseCase,
    private val getReadingReviewStateUseCase: GetVocabPracticeReadingDataUseCase,
    private val getWritingReviewStateUseCase: GetVocabPracticeWritingDataUseCase,
    private val getSummaryItemUseCase: GetVocabPracticeSummaryItemUseCase
) : VocabPracticeQueue {

    private lateinit var queue: MutableList<VocabPracticeQueueItem>

    private lateinit var practiceStartInstant: Instant
    private val summaryItems = mutableListOf<VocabSummaryItem>()

    private val nextRequests = Channel<SrsCard>()

    private val _state = MutableStateFlow<VocabReviewQueueState>(
        value = VocabReviewQueueState.Loading
    )

    override val state: StateFlow<VocabReviewQueueState>
        get() = _state

    init {
        nextRequests.consumeAsFlow()
            .debounceFirst()
            .onEach { handleAnswer(it) }
            .launchIn(coroutineScope)
    }

    override suspend fun initialize(expressions: List<VocabQueueItemDescriptor>) {
        practiceStartInstant = timeUtils.now()
        queue = expressions.map { it.toQueueItem() }.toMutableList()
        updateState()
    }

    override suspend fun completeCurrentReview(srsItem: SrsCard) {
        nextRequests.send(srsItem)
    }

    override fun finishPractice() {
        _state.value = VocabReviewQueueState.Summary(
            duration = timeUtils.now() - practiceStartInstant,
            items = summaryItems
        )
    }

    private fun getProgress(): VocabQueueProgress {
        return VocabQueueProgress(
            pending = queue.count { it.repeats == 0 },
            repeats = queue.count { it.repeats > 0 },
            completed = summaryItems.size
        )
    }

    private suspend fun handleAnswer(srsCard: SrsCard) {
        val item = queue.removeFirstOrNull() ?: return
        if (srsCard.interval < 1.days) {
            placeItemBackToQueue(item, srsCard)
        } else {
            addSummaryItem(item.copy(srsCard = srsCard))
        }

        updateState()
        srsItemRepository.update(item.srsCardKey, srsCard)
    }

    private suspend fun updateState() {
        val item = queue.getOrNull(0)
        if (item == null) {
            finishPractice()
        } else {
            if (!item.deferredState.isCompleted) {
                _state.value = VocabReviewQueueState.Loading
            }
            val time = timeUtils.now()
            val srsAnswers = srsScheduler.answers(item.srsCard, time)
            _state.value = VocabReviewQueueState.Review(
                progress = getProgress(),
                state = item.deferredState.await().toReviewState(coroutineScope),
                answers = VocabPracticeSrsAnswers(
                    again = srsAnswers.again,
                    hard = srsAnswers.hard,
                    good = srsAnswers.good,
                    easy = srsAnswers.easy
                )
            )

            queue.getOrNull(1)?.apply {
                deferredState.start()
            }
        }
    }

    private suspend fun VocabQueueItemDescriptor.toQueueItem(): VocabPracticeQueueItem {
        val srsCardKey = practiceType.toSrsItemKey(wordId)
        return VocabPracticeQueueItem(
            descriptor = this,
            srsCardKey = srsCardKey,
            srsCard = srsItemRepository.get(srsCardKey) ?: srsScheduler.newCard(),
            repeats = 0,
            deferredState = coroutineScope.async(Dispatchers.IO, start = CoroutineStart.LAZY) {
                when (this@toQueueItem) {
                    is VocabQueueItemDescriptor.Flashcard -> {
                        getFlashcardReviewStateUseCase(this@toQueueItem)
                    }

                    is VocabQueueItemDescriptor.ReadingPicker -> {
                        getReadingReviewStateUseCase(this@toQueueItem)
                    }

                    is VocabQueueItemDescriptor.Writing -> {
                        getWritingReviewStateUseCase(this@toQueueItem)
                    }
                }
            }
        )
    }

    private fun placeItemBackToQueue(
        queueItem: VocabPracticeQueueItem,
        updatedSrsItem: SrsCard
    ) {
        val nextReviewTime = getExpectedReviewTime(updatedSrsItem)
        val insertPosition = queue.asSequence()
            .map { getExpectedReviewTime(it.srsCard) }
            .indexOfFirst { nextReviewTime < it }
            .takeIf { it != -1 }
            ?.let {
                if (it == 0 && queue.size > 0) min(2, queue.size)
                else min(it, 10)
            }
            ?: min(queue.size, 10)

        val updatedQueueItem = queueItem.copy(
            srsCard = updatedSrsItem,
            repeats = queueItem.repeats + 1
        )
        queue.add(insertPosition, updatedQueueItem)
    }

    private fun getExpectedReviewTime(srsItem: SrsCard): Instant {
        return (srsItem.lastReview ?: Instant.DISTANT_PAST) + srsItem.interval
    }

    private suspend fun addSummaryItem(queueItem: VocabPracticeQueueItem) {
        val summaryItem = getSummaryItemUseCase(queueItem)//todo
        summaryItems.add(summaryItem)
    }

}