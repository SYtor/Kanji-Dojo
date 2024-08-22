package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueItemDescriptor
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType

interface GetVocabPracticeQueueDataUseCase {
    suspend operator fun invoke(
        configuration: VocabPracticeScreenConfiguration,
        state: ScreenState.Configuration
    ): List<VocabPracticeQueueItemDescriptor>
}

class DefaultGetVocabPracticeQueueDataUseCase : GetVocabPracticeQueueDataUseCase {

    override suspend fun invoke(
        configuration: VocabPracticeScreenConfiguration,
        state: ScreenState.Configuration
    ): List<VocabPracticeQueueItemDescriptor> {
        return configuration.wordIdToDeckIdMap.asSequence()
            .map { (wordId, deckId) ->
                when (state.practiceType) {
                    ScreenVocabPracticeType.Flashcard -> {
                        VocabPracticeQueueItemDescriptor.Flashcard(
                            wordId = wordId,
                            deckId = deckId,
                            priority = state.readingPriority.value,
                            translationInFont = state.flashcard.translationInFront.value
                        )
                    }

                    ScreenVocabPracticeType.ReadingPicker -> {
                        VocabPracticeQueueItemDescriptor.ReadingPicker(
                            wordId = wordId,
                            deckId = deckId,
                            priority = state.readingPriority.value,
                            showMeaning = state.readingPicker.showMeaning.value
                        )
                    }

                    ScreenVocabPracticeType.Writing -> {
                        VocabPracticeQueueItemDescriptor.Writing(
                            wordId = wordId,
                            deckId = deckId,
                            priority = state.readingPriority.value
                        )
                    }
                }
            }
            .let { if (state.shuffle.value) it.shuffled() else it }
            .toList()
    }

}