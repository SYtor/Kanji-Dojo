package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.debounceFirst
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.MutableVocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabQueueProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabFlashcardReviewStateUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeSummaryItemUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabReadingReviewStateUseCase


interface VocabPracticeQueue {
    val state: StateFlow<VocabReviewQueueState>

    suspend fun initialize(expressions: List<VocabQueueItemDescriptor>)
    suspend fun completeCurrentReview()
}

private data class QueueItem(
    val item: VocabQueueItemDescriptor,
    val deferredState: Deferred<MutableVocabReviewState>
)

class DefaultVocabPracticeQueue(
    private val coroutineScope: CoroutineScope,
    private val timeUtils: TimeUtils,
    private val getFlashcardReviewStateUseCase: GetVocabFlashcardReviewStateUseCase,
    private val getReadingReviewStateUseCase: GetVocabReadingReviewStateUseCase,
    private val getSummaryItemUseCase: GetVocabPracticeSummaryItemUseCase
) : VocabPracticeQueue {

    private lateinit var queue: MutableList<QueueItem>

    private lateinit var practiceStartInstant: Instant
    private val summaryItems = mutableListOf<VocabSummaryItem>()

    private val nextRequests = Channel<Unit>()

    private val _state = MutableStateFlow<VocabReviewQueueState>(
        value = VocabReviewQueueState.Loading
    )

    override val state: StateFlow<VocabReviewQueueState>
        get() = _state

    init {
        nextRequests.consumeAsFlow()
            .debounceFirst()
            .onEach { handleAnswer() }
            .launchIn(coroutineScope)
    }

    override suspend fun initialize(expressions: List<VocabQueueItemDescriptor>) {
        practiceStartInstant = timeUtils.now()
        queue = expressions.asSequence()
            .map { it.toQueueItem() }
            .toMutableList()
        updateState()
    }

    override suspend fun completeCurrentReview() {
        nextRequests.send(Unit)
    }

    private fun getProgress(): VocabQueueProgress {
        val total = queue.size + summaryItems.size
        val current = total - queue.size + 1
        return VocabQueueProgress(current, total)
    }

    private suspend fun handleAnswer() {
        val item = queue.removeFirstOrNull() ?: return
        val state = item.deferredState.await()
        val summaryItem = getSummaryItemUseCase(state)
        summaryItems.add(summaryItem)

        updateState()
    }

    private suspend fun updateState() {
        val item = queue.getOrNull(0)
        if (item == null) {
            _state.value = VocabReviewQueueState.Summary(
                duration = Clock.System.now() - practiceStartInstant,
                items = summaryItems
            )
        } else {
            if (!item.deferredState.isCompleted) {
                _state.value = VocabReviewQueueState.Loading
            }
            _state.value = VocabReviewQueueState.Review(
                progress = getProgress(),
                state = item.deferredState.await()
            )
            queue.getOrNull(1)?.deferredState?.start()
        }
    }

    private fun VocabQueueItemDescriptor.toQueueItem(): QueueItem {
        return QueueItem(
            item = this,
            deferredState = coroutineScope.async(start = CoroutineStart.LAZY) {
                when (this@toQueueItem) {
                    is VocabQueueItemDescriptor.Flashcard -> {
                        getFlashcardReviewStateUseCase(this@toQueueItem)
                    }

                    is VocabQueueItemDescriptor.ReadingPicker -> {
                        getReadingReviewStateUseCase(this@toQueueItem)
                    }
                }
            }
        )
    }

}