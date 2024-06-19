package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import androidx.compose.runtime.mutableStateOf
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.withEmptyFurigana
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.MutableVocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabQueueItemDescriptor

interface GetVocabFlashcardReviewStateUseCase {
    suspend operator fun invoke(
        descriptor: VocabQueueItemDescriptor.Flashcard
    ): MutableVocabReviewState.Flashcard
}

class DefaultGetVocabFlashcardReviewStateUseCase(
    private val appDataRepository: AppDataRepository,
    private val getPrioritizedWordReadingUseCase: GetPrioritizedWordReadingUseCase
) : GetVocabFlashcardReviewStateUseCase {

    override suspend fun invoke(
        descriptor: VocabQueueItemDescriptor.Flashcard
    ): MutableVocabReviewState.Flashcard {
        val word = appDataRepository.getWord(descriptor.wordId)
        val reading = getPrioritizedWordReadingUseCase(word, descriptor.priority)
        val translation = word.meanings.first()

        return MutableVocabReviewState.Flashcard(
            word = word,
            reading = reading,
            noFuriganaReading = reading.withEmptyFurigana(),
            meaning = translation,
            showMeaningInFront = descriptor.translationInFont,
            showAnswer = mutableStateOf(false)
        )
    }

}