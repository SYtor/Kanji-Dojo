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
        return when (val state = item.deferredState.getCompleted()) {
            is VocabPracticeItemData.Flashcard -> VocabSummaryItem.Flashcard(
                word = state.word,
                reading = state.reading
            )

            is VocabPracticeItemData.Reading -> VocabSummaryItem.ReadingPicker(
                word = state.word,
                reading = state.revealedReading,
                character = state.questionCharacter,
                isCorrect = true // todo
            )

            is VocabPracticeItemData.Writing -> VocabSummaryItem.Writing(
                word = state.word,
                reading = state.summaryReading,
                results = emptyList() // todo
            )
        }
    }

}