package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.runtime.State
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum

enum class VocabPracticeType : DisplayableEnum {
    ReadingPicker;

    override val titleResolver: StringResolveScope<String> = { name }
}

enum class VocabPracticeReadingPriority : DisplayableEnum {
    Default, Kanji, Kana;

    override val titleResolver: StringResolveScope<String> = { name }
}

data class VocabPracticeConfiguration(
    val practiceType: VocabPracticeType,
    val readingPriority: VocabPracticeReadingPriority,
    val showMeaning: Boolean
)

sealed interface VocabReviewState {
    interface Reading : VocabReviewState {
        val word: JapaneseWord
        val questionCharacter: String
        val displayReading: State<FuriganaString>
        val answers: List<String>
        val correctAnswer: String
        val selectedAnswer: State<SelectedReadingAnswer?>
    }
}

data class SelectedReadingAnswer(
    val selected: String,
    val correct: String
) {
    val isCorrect = selected == correct
}

data class VocabSummaryItem(
    val word: JapaneseWord,
    val reading: FuriganaString,
    val character: String,
    val isCorrect: Boolean
)