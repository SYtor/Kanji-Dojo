package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType

interface GetVocabPracticeQueueDataUseCase {
    suspend operator fun invoke(
        words: List<Long>,
        state: ScreenState.Configuration
    ): List<VocabPracticeQueueItemDescriptor>
}

class DefaultGetVocabPracticeQueueDataUseCase : GetVocabPracticeQueueDataUseCase {

    override suspend fun invoke(
        words: List<Long>,
        state: ScreenState.Configuration
    ): List<VocabPracticeQueueItemDescriptor> {
        return words.asSequence()
            .map {
                when (state.practiceType) {
                    VocabPracticeType.Flashcard -> {
                        VocabPracticeQueueItemDescriptor.Flashcard(
                            wordId = it,
                            priority = state.readingPriority.value,
                            translationInFont = state.flashcard.translationInFront.value
                        )
                    }

                    VocabPracticeType.ReadingPicker -> {
                        VocabPracticeQueueItemDescriptor.ReadingPicker(
                            wordId = it,
                            priority = state.readingPriority.value,
                            showMeaning = state.readingPicker.showMeaning.value
                        )
                    }

                    VocabPracticeType.Writing -> {
                        VocabPracticeQueueItemDescriptor.Writing(
                            wordId = it,
                            priority = state.readingPriority.value
                        )
                    }
                }
            }
            .let { if (state.shuffle.value) it.shuffled() else it }
            .toList()
    }

}