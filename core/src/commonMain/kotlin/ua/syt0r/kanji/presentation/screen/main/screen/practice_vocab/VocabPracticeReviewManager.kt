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
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabReadingReviewStateUseCase

class VocabPracticeReviewManager(
    private val coroutineScope: CoroutineScope,
    private val timeUtils: TimeUtils,
    private val getVocabReadingReviewStateUseCase: GetVocabReadingReviewStateUseCase
) {

    private data class QueueItem(
        val item: VocabQueueItemDescriptor,
        val data: Deferred<VocabReviewManagingState>
    )

    private lateinit var queue: MutableList<QueueItem>
    private lateinit var practiceStartInstant: Instant

    private val nextRequests = Channel<Unit>()

    private val _currentState = MutableStateFlow<VocabReviewManagingState>(
        value = VocabReviewManagingState.Loading
    )

    val currentState: StateFlow<VocabReviewManagingState>
        get() = _currentState

    init {
        nextRequests.consumeAsFlow()
            .debounceFirst()
            .onEach { handleNext() }
            .launchIn(coroutineScope)
    }

    suspend fun initialize(expressions: List<VocabQueueItemDescriptor>) {
        practiceStartInstant = timeUtils.now()
        queue = expressions.toQueue()
        updateState()
    }

    suspend fun next() {
        nextRequests.send(Unit)
    }

    private suspend fun handleNext() {
        queue.removeFirstOrNull() ?: return
        updateState()
    }

    private suspend fun updateState() {
        val item = queue.getOrNull(0)
        if (item == null) {
            _currentState.value = VocabReviewManagingState.Summary(
                duration = Clock.System.now() - practiceStartInstant
            )
        } else {
            if (!item.data.isCompleted) {
                _currentState.value = VocabReviewManagingState.Loading
            }
            _currentState.value = item.data.await()
            queue.getOrNull(1)?.data?.start()
        }
    }

    private fun List<VocabQueueItemDescriptor>.toQueue(): MutableList<QueueItem> {
        return map {
            QueueItem(
                item = it,
                data = it.getData()
            )
        }.toMutableList()
    }

    private fun VocabQueueItemDescriptor.getData(): Deferred<VocabReviewManagingState> {
        return coroutineScope.async(start = CoroutineStart.LAZY) {
            getVocabReadingReviewStateUseCase(id)
        }
    }

}