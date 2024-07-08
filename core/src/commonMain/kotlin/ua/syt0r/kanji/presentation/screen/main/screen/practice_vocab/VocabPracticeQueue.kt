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
import ua.syt0r.kanji.core.srs.SrsItemData
import ua.syt0r.kanji.core.srs.fsrs.FsrsAnswer
import ua.syt0r.kanji.core.srs.fsrs.FsrsItemData
import ua.syt0r.kanji.core.srs.fsrs.FsrsItemRepository
import ua.syt0r.kanji.core.srs.fsrs.FsrsScheduler
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
    suspend fun completeCurrentReview(srsItem: SrsItemData)
}

data class VocabPracticeQueueItem(
    val descriptor: VocabQueueItemDescriptor,
    val srsItem: SrsItemData,
    val repeats: Int,
    val deferredState: Deferred<VocabPracticeItemData>
)

class DefaultVocabPracticeQueue(
    private val coroutineScope: CoroutineScope,
    private val timeUtils: TimeUtils,
    private val fsrsItemRepository: FsrsItemRepository,
    private val srsScheduler: FsrsScheduler,
    private val getFlashcardReviewStateUseCase: GetVocabPracticeFlashcardDataUseCase,
    private val getReadingReviewStateUseCase: GetVocabPracticeReadingDataUseCase,
    private val getWritingReviewStateUseCase: GetVocabPracticeWritingDataUseCase,
    private val getSummaryItemUseCase: GetVocabPracticeSummaryItemUseCase
) : VocabPracticeQueue {

    private lateinit var queue: MutableList<VocabPracticeQueueItem>

    private lateinit var practiceStartInstant: Instant
    private val summaryItems = mutableListOf<VocabSummaryItem>()

    private val nextRequests = Channel<SrsItemData>()

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

    override suspend fun completeCurrentReview(srsItem: SrsItemData) {
        nextRequests.send(srsItem)
    }

    private fun getProgress(): VocabQueueProgress {
        return VocabQueueProgress(
            pending = queue.count { it.repeats == 0 },
            repeats = queue.count { it.repeats > 0 },
            completed = summaryItems.size
        )
    }

    private suspend fun handleAnswer(srsItem: SrsItemData) {
        val item = queue.removeFirstOrNull() ?: return
        if (srsItem.interval < 1.days) {
            placeItemBackToQueue(item, srsItem)
        } else {
            addSummaryItem(item)
        }

        updateState()
        fsrsItemRepository.update(item.srsItem as FsrsItemData)
    }

    private suspend fun updateState() {
        val item = queue.getOrNull(0)
        if (item == null) {
            _state.value = VocabReviewQueueState.Summary(
                duration = timeUtils.now() - practiceStartInstant,
                items = summaryItems
            )
        } else {
            if (!item.deferredState.isCompleted) {
                _state.value = VocabReviewQueueState.Loading
            }
            val time = timeUtils.now()
            _state.value = VocabReviewQueueState.Review(
                progress = getProgress(),
                state = item.deferredState.await().toReviewState(coroutineScope),
                answers = VocabPracticeSrsAnswers(
                    good = srsScheduler.get(item.srsItem, FsrsAnswer.Good, time),
                    bad = srsScheduler.get(item.srsItem, FsrsAnswer.Again, time)
                )
            )

            queue.getOrNull(1)?.apply {
                deferredState.start()
            }
        }
    }

    private suspend fun VocabQueueItemDescriptor.toQueueItem(): VocabPracticeQueueItem {
        return VocabPracticeQueueItem(
            descriptor = this,
            srsItem = fsrsItemRepository.get(practiceType.toSrsItemKey(wordId)),
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
        updatedSrsItem: SrsItemData
    ) {
        val nextReviewTime = getExpectedReviewTime(updatedSrsItem)
        val insertPosition = queue.asSequence()
            .map { getExpectedReviewTime(it.srsItem) }
            .indexOfFirst { nextReviewTime < it }
            .takeIf { it != -1 }
            ?.let {
                if (it == 0 && queue.size > 0) min(2, queue.size)
                else min(it, 10)
            }
            ?: min(queue.size, 10)

        val updatedQueueItem = queueItem.copy(
            srsItem = updatedSrsItem,
            repeats = queueItem.repeats + 1
        )
        queue.add(insertPosition, updatedQueueItem)
    }

    private fun getExpectedReviewTime(srsItem: SrsItemData): Instant {
        return (srsItem.lastReview ?: Instant.DISTANT_PAST) + srsItem.interval
    }

    private suspend fun addSummaryItem(queueItem: VocabPracticeQueueItem) {
        val summaryItem = getSummaryItemUseCase(queueItem)//todo
        summaryItems.add(summaryItem)
    }

}