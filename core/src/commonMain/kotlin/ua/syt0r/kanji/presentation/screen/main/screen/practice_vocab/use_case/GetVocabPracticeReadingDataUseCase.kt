package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.core.VocabCardResolver
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.buildFuriganaString
import ua.syt0r.kanji.core.app_data.data.formattedVocabReading
import ua.syt0r.kanji.core.app_data.data.toFurigana
import ua.syt0r.kanji.core.app_data.data.withEncodedText
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeQueueItemDescriptor

interface GetVocabPracticeReadingDataUseCase {
    suspend operator fun invoke(
        descriptor: VocabPracticeQueueItemDescriptor.ReadingPicker
    ): VocabPracticeItemData.Reading
}

class DefaultGetVocabPracticeReadingDataUseCase(
    private val vocabCardResolver: VocabCardResolver,
    private val appDataRepository: AppDataRepository,
) : GetVocabPracticeReadingDataUseCase {

    override suspend fun invoke(
        descriptor: VocabPracticeQueueItemDescriptor.ReadingPicker
    ): VocabPracticeItemData.Reading {
        val card = vocabCardResolver.resolveUserCard(descriptor.cardId)

        val revealedReading: FuriganaString
        val hiddenReading: FuriganaString

        val question: String
        val correctAnswer: String

        var forceShowMeaning = false

        when {
            card.furigana != null -> {
                revealedReading = card.furigana
                val testCompound = revealedReading.compounds
                    .filter { it.annotation != null }
                    .random()
                question = testCompound.text
                correctAnswer = testCompound.annotation!!
                hiddenReading = revealedReading.withEncodedText(correctAnswer)
            }

            card.kanjiReading != null -> {
                revealedReading = formattedVocabReading(card.kanaReading, card.kanjiReading)
                hiddenReading = buildFuriganaString { append(card.kanjiReading) }
                question = card.kanaReading
                correctAnswer = card.kanaReading
            }

            else -> {
                val letter = card.kanaReading.random().toString()
                question = letter
                correctAnswer = letter
                revealedReading = card.kanaReading.toFurigana()
                hiddenReading = revealedReading.withEncodedText(letter)
                forceShowMeaning = card.kanaReading.length == 1
            }
        }

        val answers = listOf(correctAnswer)
            .plus(getSimilarKanjiReadings(correctAnswer))
            .distinct()
            .take(ANSWERS_COUNT)
            .shuffled()

        return VocabPracticeItemData.Reading(
            question = question,
            revealedReading = revealedReading,
            hiddenReading = hiddenReading,
            meaning = card.glossary.joinToString(),
            answers = answers,
            correctAnswer = correctAnswer,
            showMeaning = descriptor.showMeaning || forceShowMeaning,
            vocabReference = null
        )
    }

    private suspend fun getSimilarKanjiReadings(text: String): List<String> {
        val readings = mutableListOf<String>()
        readings.addAll(
            appDataRepository.getCharacterReadingsOfLength(text.length, ANSWERS_COUNT)
        )
        if (text.length > 1)
            readings.addAll(
                appDataRepository.getCharacterReadingsOfLength(text.length - 1, ANSWERS_COUNT)
            )
        return readings.shuffled()
    }

    companion object {
        private const val ANSWERS_COUNT = 8
    }

}