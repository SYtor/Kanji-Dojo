package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import kotlinx.coroutines.ExperimentalCoroutinesApi
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabSummaryItem

interface GetVocabPracticeSummaryItemUseCase {
    operator fun invoke(item: VocabPracticeQueueItem): VocabSummaryItem
}

class DefaultGetVocabPracticeSummaryItemUseCase : GetVocabPracticeSummaryItemUseCase {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke(item: VocabPracticeQueueItem): VocabSummaryItem {
        return when (val state = item.data.getCompleted()) {
            is VocabPracticeItemData.Flashcard -> VocabSummaryItem(
                word = state.word,
                reading = state.reading,
                nextInterval = item.srsCard.interval
            )

            is VocabPracticeItemData.Reading -> VocabSummaryItem(
                word = state.word,
                reading = state.revealedReading,
                nextInterval = item.srsCard.interval
            )

            is VocabPracticeItemData.Writing -> VocabSummaryItem(
                word = state.word,
                reading = state.summaryReading,
                nextInterval = item.srsCard.interval
            )
        }
    }

}