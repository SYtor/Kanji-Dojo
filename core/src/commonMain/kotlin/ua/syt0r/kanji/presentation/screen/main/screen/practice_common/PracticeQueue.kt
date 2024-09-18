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
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.debounceFirst
import ua.syt0r.kanji.core.srs.SrsAnswers
import ua.syt0r.kanji.core.srs.SrsCard
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.SrsItemRepository
import ua.syt0r.kanji.core.srs.SrsScheduler
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryItem
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryRepository
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days


interface PracticeQueue<State, Descriptor> {

    val state: StateFlow<State>

    suspend fun initialize(items: List<Descriptor>)
    suspend fun submitAnswer(answer: PracticeAnswer)
    fun immediateFinish()

}

interface PracticeQueueItem<T : PracticeQueueItem<T>> {

    val srsCardKey: SrsCardKey
    val srsCard: SrsCard
    val deckId: Long
    val repeats: Int
    val totalMistakes: Int
    val data: Deferred<Any>

    fun copyForRepeat(answer: PracticeAnswer): T

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
    protected val srsScheduler: SrsScheduler,
    protected val srsItemRepository: SrsItemRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val analyticsManager: AnalyticsManager
) : PracticeQueue<State, Descriptor>
        where QueueItem : PracticeQueueItem<QueueItem>,
              SummaryItem : PracticeSummaryItem {

    protected open lateinit var queue: MutableList<QueueItem>

    protected lateinit var practiceStartInstant: Instant
    private lateinit var currentReviewStartInstant: Instant

    protected val summaryItems = mutableMapOf<SrsCardKey, SummaryItem>()

    private val submittedAnswersChannel = Channel<PracticeAnswer>()

    private val _state: MutableStateFlow<State> = MutableStateFlow(value = this.getLoadingState())
    override val state: StateFlow<State>
        get() = _state

    init {
        submittedAnswersChannel.consumeAsFlow()
            .debounceFirst()
            .onEach { handleAnswer(it) }
            .launchIn(coroutineScope)
    }

    protected abstract suspend fun Descriptor.toQueueItem(): QueueItem
    protected abstract fun createSummaryItem(queueItem: QueueItem): SummaryItem

    protected abstract fun getLoadingState(): State
    protected abstract suspend fun getReviewState(item: QueueItem, answers: PracticeAnswers): State
    protected abstract fun getSummaryState(): State

    override suspend fun initialize(items: List<Descriptor>) {
        practiceStartInstant = timeUtils.now()
        queue = items.map { it.toQueueItem() }.toMutableList()
        updateState()
    }

    override suspend fun submitAnswer(answer: PracticeAnswer) {
        submittedAnswersChannel.send(answer)
    }

    override fun immediateFinish() {
        _state.value = getSummaryState()
    }

    protected fun getProgress(): PracticeQueueProgress {
        return PracticeQueueProgress(
            pending = queue.count { it.repeats == 0 },
            repeats = queue.count { it.repeats > 0 },
            completed = summaryItems.filter { it.value.nextInterval >= 1.days }.size
        )
    }

    private fun getAnswers(answers: SrsAnswers): PracticeAnswers {
        return PracticeAnswers(
            again = PracticeAnswer(answers.again),
            hard = PracticeAnswer(answers.hard),
            good = PracticeAnswer(answers.good),
            easy = PracticeAnswer(answers.easy)
        )
    }

    private suspend fun handleAnswer(answer: PracticeAnswer) {
        val item = queue.removeFirstOrNull() ?: return
        val updatedItem = item.copyForRepeat(answer)

        saveSummaryData(updatedItem)

        val instant = timeUtils.now()
        val reviewDuration = instant - currentReviewStartInstant

        if (answer.srsAnswer.card.interval < 1.days) {
            placeItemBackToQueue(updatedItem)
        }

        updateState()

        srsItemRepository.update(item.srsCardKey, answer.srsAnswer.card)
        saveReviewHistory(item, answer, instant, reviewDuration)
        reportReview(updatedItem, answer, reviewDuration)
    }

    private suspend fun updateState() {
        val item = queue.getOrNull(0)
        if (item == null) {
            immediateFinish()
        } else {
            if (!item.data.isCompleted) {
                _state.value = getLoadingState()
            }
            val time = timeUtils.now()
            val srsAnswers = srsScheduler.answers(item.srsCard, time)

            item.data.await()
            currentReviewStartInstant = timeUtils.now()

            _state.value = getReviewState(item, getAnswers(srsAnswers))

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
                if (it == 0 && queue.size > 0) min(MIN_QUEUE_POSITION_SHIFT - 1, queue.size)
                else min(it, MAX_QUEUE_POSITION_SHIFT - 1)
            }
            ?: min(queue.size, MAX_QUEUE_POSITION_SHIFT - 1)

        queue.add(insertPosition, updatedQueueItem)
    }

    private fun getExpectedReviewTime(srsItem: SrsCard): Instant {
        return (srsItem.lastReview ?: Instant.DISTANT_PAST) + srsItem.interval
    }

    private fun saveSummaryData(queueItem: QueueItem) {
        val summaryItem = createSummaryItem(queueItem)
        summaryItems[queueItem.srsCardKey] = summaryItem
    }

    private suspend fun saveReviewHistory(
        queueItem: QueueItem,
        answer: PracticeAnswer,
        reviewStart: Instant,
        reviewDuration: Duration
    ) {
        val item = ReviewHistoryItem(
            key = queueItem.srsCardKey.itemKey,
            practiceType = queueItem.srsCardKey.practiceType,
            timestamp = reviewStart,
            duration = reviewDuration,
            grade = answer.srsAnswer.grade,
            mistakes = answer.mistakes,
            deckId = queueItem.deckId
        )
        reviewHistoryRepository.addReview(item)
    }

    private fun reportReview(
        item: QueueItem,
        answer: PracticeAnswer,
        reviewDuration: Duration
    ) {
        analyticsManager.sendEvent("review") {
            put("key", item.srsCardKey.itemKey)
            put("practice_type", item.srsCardKey.practiceType)
            put("duration", reviewDuration.inWholeMilliseconds)
            put("mistakes", answer.mistakes)
            put("repeats", item.srsCard.fsrsCard.repeats)
            put("lapses", item.srsCard.fsrsCard.lapses)
        }
    }

    companion object {
        private const val MIN_QUEUE_POSITION_SHIFT = 3
        private const val MAX_QUEUE_POSITION_SHIFT = 10
    }

}
