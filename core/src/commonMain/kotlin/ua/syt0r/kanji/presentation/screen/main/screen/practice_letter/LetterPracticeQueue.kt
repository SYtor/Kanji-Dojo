package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import ua.syt0r.kanji.core.srs.SrsItemRepository
import ua.syt0r.kanji.core.srs.SrsScheduler
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryItem
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryRepository
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.BasePracticeQueue
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueue
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeQueueItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case.GetLetterPracticeQueueItemDataUseCase

typealias LetterPracticeQueue = PracticeQueue<LetterPracticeQueueState, LetterPracticeQueueItemDescriptor>
typealias BaseLetterPracticeQueue = BasePracticeQueue<LetterPracticeQueueState, LetterPracticeQueueItemDescriptor, LetterPracticeQueueItem, LetterPracticeSummaryItem>

class DefaultLetterPracticeQueue(
    private val coroutineScope: CoroutineScope,
    timeUtils: TimeUtils,
    srsItemRepository: SrsItemRepository,
    srsScheduler: SrsScheduler,
    private val getQueueItemDataUseCase: GetLetterPracticeQueueItemDataUseCase,
    private val reviewHistoryRepository: ReviewHistoryRepository
) : BaseLetterPracticeQueue(coroutineScope, timeUtils, srsItemRepository, srsScheduler),
    LetterPracticeQueue {

    override suspend fun LetterPracticeQueueItemDescriptor.toQueueItem(): LetterPracticeQueueItem {
        val srsCardKey = practiceType.toSrsKey(character)
        return LetterPracticeQueueItem(
            descriptor = this,
            srsCardKey = srsCardKey,
            srsCard = srsItemRepository.get(srsCardKey) ?: srsScheduler.newCard(),
            deckId = deckId,
            repeats = 0,
            totalMistakes = mutableStateOf(0),
            data = coroutineScope.async(
                context = Dispatchers.IO,
                start = CoroutineStart.LAZY
            ) {
                getQueueItemDataUseCase(this@toQueueItem)
            }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun createSummaryItem(queueItem: LetterPracticeQueueItem): LetterPracticeSummaryItem {
        return when (val data = queueItem.data.getCompleted()) {
            is LetterPracticeItemData.WritingData -> {
                LetterPracticeSummaryItem.Writing(
                    letter = queueItem.descriptor.character,
                    nextInterval = queueItem.srsCard.interval,
                    strokeCount = data.strokes.size,
                    mistakes = queueItem.totalMistakes.value,
                )
            }

            is LetterPracticeItemData.ReadingData -> {
                LetterPracticeSummaryItem.Reading(
                    letter = queueItem.descriptor.character,
                    nextInterval = queueItem.srsCard.interval
                )
            }

            else -> error("Unsupported")
        }
    }

    override suspend fun saveReviewHistory(
        queueItem: LetterPracticeQueueItem,
        answer: PracticeAnswer
    ) {
        val instant = timeUtils.now()
        val item = ReviewHistoryItem(
            key = queueItem.srsCardKey.itemKey,
            practiceType = queueItem.srsCardKey.practiceType,
            timestamp = instant,
            duration = instant - currentReviewStartInstant,
            grade = answer.srsAnswer.grade,
            mistakes = queueItem.currentReviewMistakes.value,
            deckId = queueItem.deckId
        )
        reviewHistoryRepository.addReview(item)
    }

    override fun getLoadingState(): LetterPracticeQueueState {
        return LetterPracticeQueueState.Loading
    }

    override suspend fun getReviewState(
        item: LetterPracticeQueueItem,
        answers: PracticeAnswers
    ): LetterPracticeQueueState {
        return LetterPracticeQueueState.Review(
            descriptor = item.descriptor,
            data = item.data.await(),
            currentItemRepeat = item.repeats,
            progress = getProgress(),
            answers = answers,
            totalMistakes = item.totalMistakes,
            currentReviewMistakes = item.currentReviewMistakes
        )
    }

    override fun getSummaryState(): LetterPracticeQueueState {
        val instant = timeUtils.now()
        return LetterPracticeQueueState.Summary(
            duration = instant - practiceStartInstant,
            items = summaryItems.values.toList()
        )
    }

}