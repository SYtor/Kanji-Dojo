package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabQueueItemDescriptor

interface GetVocabPracticeQueueDataUseCase {
    suspend operator fun invoke(
        words: List<Long>,
        state: ScreenState.Configuration
    ): List<VocabQueueItemDescriptor>
}

class DefaultGetVocabPracticeQueueDataUseCase : GetVocabPracticeQueueDataUseCase {

    override suspend fun invoke(
        words: List<Long>,
        state: ScreenState.Configuration
    ): List<VocabQueueItemDescriptor> {
        return words.asSequence()
            .map {
                when (state.practiceType.value) {
                    VocabPracticeType.Flashcard -> {
                        VocabQueueItemDescriptor.Flashcard(
                            wordId = it,
                            priority = state.readingPriority.value,
                            translationInFont = state.flashcard.translationInFront.value
                        )
                    }

                    VocabPracticeType.ReadingPicker -> {
                        VocabQueueItemDescriptor.ReadingPicker(
                            wordId = it,
                            priority = state.readingPriority.value,
                            showMeaning = state.readingPicker.showMeaning.value
                        )
                    }

                    VocabPracticeType.Writing -> {
                        VocabQueueItemDescriptor.Writing(
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