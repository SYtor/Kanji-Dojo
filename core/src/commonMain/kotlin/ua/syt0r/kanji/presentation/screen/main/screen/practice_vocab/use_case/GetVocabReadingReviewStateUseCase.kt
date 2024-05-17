package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.withEncodedText
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabReviewManagingState

interface GetVocabReadingReviewStateUseCase {
    suspend operator fun invoke(id: Long): VocabReviewManagingState.Reading
}

class DefaultGetVocabReadingReviewStateUseCase(
    private val appDataRepository: AppDataRepository
) : GetVocabReadingReviewStateUseCase {

    override suspend fun invoke(id: Long): VocabReviewManagingState.Reading {
        val word = appDataRepository.getWord(id)

        val kanjiReading = word.readings.find { it.compounds.any { it.annotation != null } }
        val practiceReading = kanjiReading ?: word.readings.first()

        val isKana = practiceReading.compounds.all { it.annotation == null } // TODO

        val (questionCharacter, correctAnswer) = when {
            isKana -> {
                practiceReading.compounds.random().text.random().toString().let { it to it }
            }

            else -> {
                practiceReading.compounds
                    .filter { it.annotation != null }
                    .random()
                    .takeIf { it.annotation != null }
                    ?.let { it.text to it.annotation!! }
                    ?: practiceReading.compounds.random().text.random().toString().let { it to it }
            }
        }

        val answers = listOf(correctAnswer)
            .plus(getSimilarKanjiReadings(correctAnswer))
            .distinct()
            .take(ANSWERS_COUNT)
            .shuffled()

        return VocabReviewManagingState.Reading(
            word = word,
            questionCharacter = questionCharacter,
            revealedReading = practiceReading,
            hiddenReading = practiceReading.withEncodedText(correctAnswer),
            answers = answers,
            correctAnswer = correctAnswer
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