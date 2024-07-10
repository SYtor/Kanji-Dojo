package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import kotlinx.coroutines.ExperimentalCoroutinesApi
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeQueueItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabSummaryItem

interface GetVocabPracticeSummaryItemUseCase {
    operator fun invoke(item: VocabPracticeQueueItem): VocabSummaryItem
}

class DefaultGetVocabPracticeSummaryItemUseCase : GetVocabPracticeSummaryItemUseCase {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke(item: VocabPracticeQueueItem): VocabSummaryItem {
        val nextReview = item.srsCard.run { lastReview!! + interval }
        return when (val state = item.deferredState.getCompleted()) {
            is VocabPracticeItemData.Flashcard -> VocabSummaryItem(
                word = state.word,
                reading = state.reading,
                nextReview = nextReview
            )

            is VocabPracticeItemData.Reading -> VocabSummaryItem(
                word = state.word,
                reading = state.revealedReading,
                nextReview = nextReview
            )

            is VocabPracticeItemData.Writing -> VocabSummaryItem(
                word = state.word,
                reading = state.summaryReading,
                nextReview = nextReview
            )
        }
    }

}