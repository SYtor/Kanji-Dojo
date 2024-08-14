package ua.syt0r.kanji.presentation.screen.main.screen.practice_common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.debounceFirst
import ua.syt0r.kanji.core.srs.SrsAnswer
import ua.syt0r.kanji.core.srs.SrsCard
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.SrsItemRepository
import ua.syt0r.kanji.core.srs.SrsScheduler
import ua.syt0r.kanji.core.time.TimeUtils
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days


interface PracticeQueue<State, Descriptor> {

    val state: StateFlow<State>

    suspend fun initialize(items: List<Descriptor>)
    suspend fun completeCurrentReview(srsItem: SrsCard)
    fun finishPractice()

}

interface PracticeQueueItem {

    val srsCardKey: SrsCardKey
    val srsCard: SrsCard
    val repeats: Int
    val data: Deferred<Any>

    fun copyForRepeat(srsCard: SrsCard): PracticeQueueItem

}

data class PracticeQueueProgress(
    val pending: Int,
    val repeats: Int,
    val completed: Int
)

interface PracticeSummaryItem {
    val nextInterval: Duration
}

abstract class BasePracticeQueue<State, Descriptor, QueueItem, SummaryItem>(
    coroutineScope: CoroutineScope,
    protected val timeUtils: TimeUtils,
    protected val srsItemRepository: SrsItemRepository,
    protected val srsScheduler: SrsScheduler,
) : PracticeQueue<State, Descriptor> where QueueItem : PracticeQueueItem, SummaryItem : PracticeSummaryItem {

    protected open lateinit var queue: MutableList<QueueItem>

    protected lateinit var practiceStartInstant: Instant
    protected val summaryItems = mutableMapOf<SrsCardKey, SummaryItem>()

    private val nextRequests = Channel<SrsCard>()

    private val _state: MutableStateFlow<State> = MutableStateFlow(value = this.getLoadingState())
    override val state: StateFlow<State>
        get() = _state

    init {
        nextRequests.consumeAsFlow()
            .debounceFirst()
            .onEach { handleAnswer(it) }
            .launchIn(coroutineScope)
    }

    protected abstract suspend fun Descriptor.toQueueItem(): QueueItem
    protected abstract fun createSummaryItem(queueItem: QueueItem): SummaryItem

    protected abstract fun getLoadingState(): State
    protected abstract suspend fun getReviewState(item: QueueItem, answers: SrsAnswer): State
    protected abstract fun getSummaryState(): State

    override suspend fun initialize(items: List<Descriptor>) {
        practiceStartInstant = timeUtils.now()
        queue = items.map { it.toQueueItem() }.toMutableList()
        updateState()
    }

    override suspend fun completeCurrentReview(srsItem: SrsCard) {
        nextRequests.send(srsItem)
    }

    override fun finishPractice() {
        _state.value = getSummaryState()
    }

    protected fun getProgress(): PracticeQueueProgress {
        return PracticeQueueProgress(
            pending = queue.count { it.repeats == 0 },
            repeats = queue.count { it.repeats > 0 },
            completed = summaryItems.filter { it.value.nextInterval >= 1.days }.size
        )
    }

    private suspend fun handleAnswer(srsCard: SrsCard) {
        val item = queue.removeFirstOrNull() ?: return
        val updatedItem = item.copyForRepeat(srsCard = srsCard) as QueueItem

        saveSummaryData(updatedItem)

        if (srsCard.interval < 1.days) {
            placeItemBackToQueue(updatedItem)
        }

        updateState()
        srsItemRepository.update(item.srsCardKey, srsCard)
    }

    private suspend fun updateState() {
        val item = queue.getOrNull(0)
        if (item == null) {
            finishPractice()
        } else {
            if (!item.data.isCompleted) {
                _state.value = getLoadingState()
            }
            val time = timeUtils.now()
            val srsAnswers = srsScheduler.answers(item.srsCard, time)
            _state.value = getReviewState(item, srsAnswers)

            queue.getOrNull(1)?.apply {
                data.start()
            }
        }
    }

    private fun placeItemBackToQueue(
        updatedQueueItem: QueueItem
    ) {
        val nextReviewTime = getExpectedReviewTime(updatedQueueItem.srsCard)
        val insertPosition = queue.asSequence()
            .map { getExpectedReviewTime(it.srsCard) }
            .indexOfFirst { nextReviewTime < it }
            .takeIf { it != -1 }
            ?.let {
                if (it == 0 && queue.size > 0) min(2, queue.size)
                else min(it, 10)
            }
            ?: min(queue.size, 10)

        queue.add(insertPosition, updatedQueueItem)
    }

    private fun getExpectedReviewTime(srsItem: SrsCard): Instant {
        return (srsItem.lastReview ?: Instant.DISTANT_PAST) + srsItem.interval
    }

    private fun saveSummaryData(queueItem: QueueItem) {
        val summaryItem = createSummaryItem(queueItem)
        summaryItems[queueItem.srsCardKey] = summaryItem
    }

}
