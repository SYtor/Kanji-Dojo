package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWritingStatus
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.MutableVocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabCharacterPracticeResult
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabCharacterWritingData
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

            is MutableVocabReviewState.Writing -> VocabSummaryItem.Writing(
                word = state.word,
                reading = state.summaryReading,
                results = state.charactersData
                    .filterIsInstance<VocabCharacterWritingData.WithStrokes>()
                    .map { writingData ->
                        VocabCharacterPracticeResult(
                            character = writingData.character,
                            isCorrect = writingData.writerState.writingStatus
                                .let { it.value as CharacterWritingStatus.Completed }
                                .isCorrect
                        )
                    }
            )
        }
    }

}