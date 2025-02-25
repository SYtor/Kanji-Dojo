package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.core.VocabCardResolver
import ua.syt0r.kanji.core.app_data.data.formattedVocabReading
import ua.syt0r.kanji.core.app_data.data.toFurigana
import ua.syt0r.kanji.core.app_data.data.withEmptyFurigana
import ua.syt0r.kanji.core.toInfoScreenData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueItemDescriptor

interface GetVocabPracticeFlashcardDataUseCase {
    suspend operator fun invoke(
        descriptor: VocabPracticeQueueItemDescriptor.Flashcard
    ): VocabPracticeItemData.Flashcard
}

class DefaultGetVocabPracticeFlashcardDataUseCase(
    private val vocabCardResolver: VocabCardResolver
) : GetVocabPracticeFlashcardDataUseCase {

    override suspend fun invoke(
        descriptor: VocabPracticeQueueItemDescriptor.Flashcard
    ): VocabPracticeItemData.Flashcard {
        val card = vocabCardResolver.resolveUserCard(descriptor.cardId)
        val revealedReading =
            card.run { formattedVocabReading(kanaReading, kanjiReading, furigana) }
        val hiddenReading = card.furigana?.withEmptyFurigana()
            ?: card.kanjiReading?.toFurigana()
            ?: card.kanaReading.toFurigana()
        return VocabPracticeItemData.Flashcard(
            reading = revealedReading,
            hiddenReading = hiddenReading,
            meaning = card.glossary.joinToString(),
            showMeaningInFront = descriptor.translationInFont,
            vocabReference = card.toInfoScreenData()
        )
    }

}