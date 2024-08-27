package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.SrsItemRepository
import ua.syt0r.kanji.core.srs.SrsScheduler
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryItem
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryRepository
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.BasePracticeQueue
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueue
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeFlashcardDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeReadingDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeSummaryItemUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case.GetVocabPracticeWritingDataUseCase

typealias VocabPracticeQueue = PracticeQueue<VocabPracticeQueueState, VocabPracticeQueueItemDescriptor>

private typealias BaseVocabPracticeQueue =
        BasePracticeQueue<VocabPracticeQueueState, VocabPracticeQueueItemDescriptor, VocabPracticeQueueItem, VocabSummaryItem>

class DefaultVocabPracticeQueue(
    private val coroutineScope: CoroutineScope,
    timeUtils: TimeUtils,
    srsItemRepository: SrsItemRepository,
    srsScheduler: SrsScheduler,
    private val getFlashcardReviewStateUseCase: GetVocabPracticeFlashcardDataUseCase,
    private val getReadingReviewStateUseCase: GetVocabPracticeReadingDataUseCase,
    private val getWritingReviewStateUseCase: GetVocabPracticeWritingDataUseCase,
    private val getSummaryItemUseCase: GetVocabPracticeSummaryItemUseCase,
    private val reviewHistoryRepository: ReviewHistoryRepository
) : BaseVocabPracticeQueue(coroutineScope, timeUtils, srsItemRepository, srsScheduler),
    VocabPracticeQueue {

    override suspend fun VocabPracticeQueueItemDescriptor.toQueueItem(): VocabPracticeQueueItem {
        val srsCardKey = practiceType.dataType.toSrsKey(wordId)
        return VocabPracticeQueueItem(
            descriptor = this,
            srsCardKey = srsCardKey,
            srsCard = srsItemRepository.get(srsCardKey) ?: srsScheduler.newCard(),
            deckId = deckId,
            repeats = 0,
            totalMistakes = 0,
            data = coroutineScope.async(Dispatchers.IO, start = CoroutineStart.LAZY) {
                when (this@toQueueItem) {
                    is VocabPracticeQueueItemDescriptor.Flashcard -> {
                        getFlashcardReviewStateUseCase(this@toQueueItem)
                    }

                    is VocabPracticeQueueItemDescriptor.ReadingPicker -> {
                        getReadingReviewStateUseCase(this@toQueueItem)
                    }

                    is VocabPracticeQueueItemDescriptor.Writing -> {
                        getWritingReviewStateUseCase(this@toQueueItem)
                    }
                }
            }
        )
    }

    override suspend fun saveReviewHistory(
        queueItem: VocabPracticeQueueItem,
        answer: PracticeAnswer,
        reviewStart: Instant
    ) {
        val instant = timeUtils.now()
        val reviewHistoryItem = ReviewHistoryItem(
            key = queueItem.srsCardKey.itemKey,
            practiceType = queueItem.srsCardKey.practiceType,
            timestamp = instant,
            duration = instant - reviewStart,
            grade = answer.srsAnswer.grade,
            mistakes = answer.mistakes,
            deckId = queueItem.deckId
        )
        reviewHistoryRepository.addReview(reviewHistoryItem)
    }

    override fun createSummaryItem(queueItem: VocabPracticeQueueItem): VocabSummaryItem {
        return getSummaryItemUseCase(queueItem)
    }

    override fun getLoadingState(): VocabPracticeQueueState = VocabPracticeQueueState.Loading

    override suspend fun getReviewState(
        item: VocabPracticeQueueItem,
        answers: PracticeAnswers
    ): VocabPracticeQueueState {
        return VocabPracticeQueueState.Review(
            progress = getProgress(),
            state = item.data.await().toReviewState(coroutineScope),
            answers = answers
        )
    }

    override fun getSummaryState(): VocabPracticeQueueState {
        return VocabPracticeQueueState.Summary(
            duration = timeUtils.now() - practiceStartInstant,
            items = summaryItems.values.toList()
        )
    }

}