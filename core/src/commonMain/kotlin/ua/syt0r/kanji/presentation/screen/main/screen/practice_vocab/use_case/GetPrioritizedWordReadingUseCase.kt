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
        // todo remove
        return word.displayReading.furiganaPreview
    }

}