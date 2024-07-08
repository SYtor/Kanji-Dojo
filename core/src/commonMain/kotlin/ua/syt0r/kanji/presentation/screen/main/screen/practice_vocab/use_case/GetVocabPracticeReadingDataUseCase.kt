package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.withEncodedText
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabQueueItemDescriptor

interface GetVocabPracticeReadingDataUseCase {
    suspend operator fun invoke(
        descriptor: VocabQueueItemDescriptor.ReadingPicker
    ): VocabPracticeItemData.Reading
}

class DefaultGetVocabPracticeReadingDataUseCase(
    private val appDataRepository: AppDataRepository,
    private val getPrioritizedWordReadingUseCase: GetPrioritizedWordReadingUseCase
) : GetVocabPracticeReadingDataUseCase {

    override suspend fun invoke(
        descriptor: VocabQueueItemDescriptor.ReadingPicker
    ): VocabPracticeItemData.Reading {
        val word = appDataRepository.getWord(descriptor.wordId)
        val reading = getPrioritizedWordReadingUseCase(word, descriptor.priority)

        val containsKanji = reading.compounds.any { it.annotation != null }

        val (questionCharacter, correctAnswer) = when {
            containsKanji -> {
                reading.compounds
                    .filter { it.annotation != null }
                    .random()
                    .let { it.text to it.annotation!! }
            }

            else -> {
                reading.compounds.random().text.random().toString().let { it to it }
            }
        }

        val answers = listOf(correctAnswer)
            .plus(getSimilarKanjiReadings(correctAnswer))
            .distinct()
            .take(ANSWERS_COUNT)
            .shuffled()

        return VocabPracticeItemData.Reading(
            word = word,
            questionCharacter = questionCharacter,
            revealedReading = reading,
            hiddenReading = reading.withEncodedText(correctAnswer),
            answers = answers,
            correctAnswer = correctAnswer,
            showMeaning = descriptor.showMeaning
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