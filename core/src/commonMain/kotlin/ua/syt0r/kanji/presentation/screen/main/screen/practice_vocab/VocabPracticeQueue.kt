package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.runtime.mutableStateOf
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
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.debounceFirst
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabReadingReviewStateUseCase
import kotlin.time.Duration


interface VocabPracticeQueue {

    val state: StateFlow<VocabReviewManagingState>

    suspend fun initialize(expressions: List<VocabQueueItemDescriptor>)
    suspend fun completeCurrentReview(isCorrectAnswer: Boolean)
    fun getProgress(): VocabQueueProgress

}

data class VocabQueueItemDescriptor(
    val id: Long,
    val practiceType: VocabPracticeType,
    val priority: VocabPracticeReadingPriority
)

data class VocabQueueProgress(
    val current: Int,
    val total: Int
)

sealed interface VocabReviewManagingState {

    object Loading : VocabReviewManagingState

    sealed interface Review : VocabReviewManagingState {

        val asVocabReviewState: VocabReviewState

        val word: JapaneseWord
        val reading: FuriganaString
        val questionCharacter: String

        fun isCorrectAnswer(): Boolean

        class Reading(
            override val word: JapaneseWord,
            override val questionCharacter: String,
            val revealedReading: FuriganaString,
            val hiddenReading: FuriganaString,
            override val answers: List<String>,
            override val correctAnswer: String,
        ) : Review, VocabReviewState.Reading {

            override val asVocabReviewState: VocabReviewState.Reading = this

            override val reading: FuriganaString = revealedReading
            override val displayReading = mutableStateOf<FuriganaString>(hiddenReading)
            override val selectedAnswer = mutableStateOf<SelectedReadingAnswer?>(null)

            override fun isCorrectAnswer(): Boolean = selectedAnswer.value!!.isCorrect

        }

    }

    data class Summary(
        val duration: Duration,
        val items: List<VocabSummaryItem>
    ) : VocabReviewManagingState

}

class DefaultVocabPracticeQueue(
    private val coroutineScope: CoroutineScope,
    private val timeUtils: TimeUtils,
    private val getVocabReadingReviewStateUseCase: GetVocabReadingReviewStateUseCase
) : VocabPracticeQueue {

    private data class QueueItem(
        val item: VocabQueueItemDescriptor,
        val state: Deferred<VocabReviewManagingState.Review>
    )

    private lateinit var queue: MutableList<QueueItem>
    private val summaryItems = mutableListOf<VocabSummaryItem>()
    private lateinit var practiceStartInstant: Instant

    private val nextRequests = Channel<Boolean>()

    private val _currentState = MutableStateFlow<VocabReviewManagingState>(
        value = VocabReviewManagingState.Loading
    )

    override val state: StateFlow<VocabReviewManagingState>
        get() = _currentState

    init {
        nextRequests.consumeAsFlow()
            .debounceFirst()
            .onEach { handleAnswer(it) }
            .launchIn(coroutineScope)
    }

    override suspend fun initialize(expressions: List<VocabQueueItemDescriptor>) {
        practiceStartInstant = timeUtils.now()
        queue = expressions.toQueue()
        updateState()
    }

    override suspend fun completeCurrentReview(isCorrectAnswer: Boolean) {
        nextRequests.send(isCorrectAnswer)
    }

    override fun getProgress(): VocabQueueProgress {
        val total = queue.size + summaryItems.size
        val current = total - queue.size + 1
        return VocabQueueProgress(current, total)
    }

    private suspend fun handleAnswer(isCorrectAnswer: Boolean) {
        val item = queue.removeFirstOrNull() ?: return
        val data = item.state.await()
        summaryItems.add(data.toSummaryItem(isCorrectAnswer))

        updateState()
    }

    private suspend fun updateState() {
        val item = queue.getOrNull(0)
        if (item == null) {
            _currentState.value = VocabReviewManagingState.Summary(
                duration = Clock.System.now() - practiceStartInstant,
                items = summaryItems
            )
        } else {
            if (!item.state.isCompleted) {
                _currentState.value = VocabReviewManagingState.Loading
            }
            _currentState.value = item.state.await()
            queue.getOrNull(1)?.state?.start()
        }
    }

    private fun List<VocabQueueItemDescriptor>.toQueue(): MutableList<QueueItem> {
        return map {
            QueueItem(
                item = it,
                state = it.getData()
            )
        }.toMutableList()
    }

    private fun VocabQueueItemDescriptor.getData(): Deferred<VocabReviewManagingState.Review> {
        return coroutineScope.async(start = CoroutineStart.LAZY) {
            when (practiceType) {
                VocabPracticeType.ReadingPicker -> getVocabReadingReviewStateUseCase(id, priority)
            }
        }
    }

    private fun VocabReviewManagingState.Review.toSummaryItem(
        isCorrectAnswer: Boolean
    ) = VocabSummaryItem(
        word = word,
        reading = reading,
        character = questionCharacter,
        isCorrect = isCorrectAnswer
    )

}