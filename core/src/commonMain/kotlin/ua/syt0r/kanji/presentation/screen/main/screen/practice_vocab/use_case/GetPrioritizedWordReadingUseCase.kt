package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.use_case

import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeReadingPriority

interface GetPrioritizedWordReadingUseCase {
    operator fun invoke(
        word: JapaneseWord,
        priority: VocabPracticeReadingPriority
    ): FuriganaString
}

class DefaultGetPrioritizedWordReadingUseCase : GetPrioritizedWordReadingUseCase {

    override fun invoke(
        word: JapaneseWord,
        priority: VocabPracticeReadingPriority
    ): FuriganaString {
        return when (priority) {
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
    }

}