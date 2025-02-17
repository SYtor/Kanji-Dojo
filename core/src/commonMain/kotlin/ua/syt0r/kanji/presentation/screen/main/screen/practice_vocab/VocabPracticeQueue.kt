package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.srs.SrsCardRepository
import ua.syt0r.kanji.core.srs.SrsScheduler
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.database.ReviewHistoryRepository
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.BasePracticeQueue
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
    srsCardRepository: SrsCardRepository,
    srsScheduler: SrsScheduler,
    private val getFlashcardReviewStateUseCase: GetVocabPracticeFlashcardDataUseCase,
    private val getReadingReviewStateUseCase: GetVocabPracticeReadingDataUseCase,
    private val getWritingReviewStateUseCase: GetVocabPracticeWritingDataUseCase,
    private val getSummaryItemUseCase: GetVocabPracticeSummaryItemUseCase,
    reviewHistoryRepository: ReviewHistoryRepository,
    analyticsManager: AnalyticsManager
) : BaseVocabPracticeQueue(
    coroutineScope = coroutineScope,
    timeUtils = timeUtils,
    srsCardRepository = srsCardRepository,
    reviewHistoryRepository = reviewHistoryRepository,
    srsScheduler = srsScheduler,
    analyticsManager = analyticsManager
), VocabPracticeQueue {

    override suspend fun VocabPracticeQueueItemDescriptor.toQueueItem(): VocabPracticeQueueItem {
        val srsCardKey = practiceType.dataType.toSrsKey(cardId)
        return VocabPracticeQueueItem(
            descriptor = this,
            srsCardKey = srsCardKey,
            srsCard = srsCardRepository.get(srsCardKey) ?: srsScheduler.newCard(),
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