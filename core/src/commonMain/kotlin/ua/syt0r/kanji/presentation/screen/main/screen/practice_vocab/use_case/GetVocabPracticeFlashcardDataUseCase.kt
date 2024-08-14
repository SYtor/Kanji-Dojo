package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.withEmptyFurigana
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueItemDescriptor

interface GetVocabPracticeFlashcardDataUseCase {
    suspend operator fun invoke(
        descriptor: VocabPracticeQueueItemDescriptor.Flashcard
    ): VocabPracticeItemData.Flashcard
}

class DefaultGetVocabPracticeFlashcardDataUseCase(
    private val appDataRepository: AppDataRepository,
    private val getPrioritizedWordReadingUseCase: GetPrioritizedWordReadingUseCase
) : GetVocabPracticeFlashcardDataUseCase {

    override suspend fun invoke(
        descriptor: VocabPracticeQueueItemDescriptor.Flashcard
    ): VocabPracticeItemData.Flashcard {
        val word = appDataRepository.getWord(descriptor.wordId)
        val reading = getPrioritizedWordReadingUseCase(word, descriptor.priority)
        val translation = word.meanings.first()

        return VocabPracticeItemData.Flashcard(
            word = word,
            reading = reading,
            noFuriganaReading = reading.withEmptyFurigana(),
            meaning = translation,
            showMeaningInFront = descriptor.translationInFont
        )
    }

}