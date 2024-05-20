package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.withEncodedText
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeReadingPriority
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabReviewManagingState

interface GetVocabReadingReviewStateUseCase {
    suspend operator fun invoke(
        wordId: Long,
        priority: VocabPracticeReadingPriority
    ): VocabReviewManagingState.Reading
}

class DefaultGetVocabReadingReviewStateUseCase(
    private val appDataRepository: AppDataRepository
) : GetVocabReadingReviewStateUseCase {

    override suspend fun invoke(
        wordId: Long,
        priority: VocabPracticeReadingPriority
    ): VocabReviewManagingState.Reading {
        val word = appDataRepository.getWord(wordId)

        val reading = when (priority) {
            VocabPracticeReadingPriority.Default -> word.readings.first()
            VocabPracticeReadingPriority.Kanji -> {
                val readingWithKanji = word.readings.find {
                    it.compounds.any { it.annotation != null }
                }
                readingWithKanji ?: word.readings.first()
            }

            VocabPracticeReadingPriority.Kana -> {
                val kanaReading = word.readings.find {
                    it.compounds.all { it.annotation == null }
                }
                kanaReading ?: word.readings.first()
            }
        }

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

        return VocabReviewManagingState.Reading(
            word = word,
            questionCharacter = questionCharacter,
            revealedReading = reading,
            hiddenReading = reading.withEncodedText(correctAnswer),
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