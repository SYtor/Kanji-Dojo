package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.MutableVocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabSummaryItem

interface GetVocabPracticeSummaryItemUseCase {
    operator fun invoke(state: MutableVocabReviewState): VocabSummaryItem
}

class DefaultGetVocabPracticeSummaryItemUseCase : GetVocabPracticeSummaryItemUseCase {

    override fun invoke(state: MutableVocabReviewState): VocabSummaryItem {
        return when (state) {
            is MutableVocabReviewState.Flashcard -> VocabSummaryItem.Flashcard(
                word = state.word,
                reading = state.summaryReading
            )

            is MutableVocabReviewState.Reading -> VocabSummaryItem.ReadingPicker(
                word = state.word,
                reading = state.summaryReading,
                character = state.questionCharacter,
                isCorrect = state.isCorrectAnswer()!!
            )

            // TODO
            is MutableVocabReviewState.Writing -> VocabSummaryItem.Flashcard(
                word = state.word,
                reading = state.summaryReading
            )
        }
    }

}