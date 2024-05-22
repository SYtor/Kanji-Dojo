package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.runtime.State
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.user_data.model.VocabReadingPriority
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum

enum class VocabPracticeType : DisplayableEnum {
    ReadingPicker;

    override val titleResolver: StringResolveScope<String> = { "Pick Reading" }
}

enum class VocabPracticeReadingPriority(
    val repoType: VocabReadingPriority
) : DisplayableEnum {

    Default(VocabReadingPriority.Default),
    Kanji(VocabReadingPriority.Kanji),
    Kana(VocabReadingPriority.Kana);

    override val titleResolver: StringResolveScope<String> = { name }

}

fun VocabReadingPriority.toScreenType(): VocabPracticeReadingPriority {
    return VocabPracticeReadingPriority.values().first { it.repoType == this }
}

data class VocabPracticeConfiguration(
    val practiceType: VocabPracticeType,
    val shuffle: Boolean,
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

data class VocabPracticeReviewState(
    val currentPositionInQueue: Int,
    val totalItemsInQueue: Int,
    val reviewState: VocabReviewState
)

data class VocabSummaryItem(
    val word: JapaneseWord,
    val reading: FuriganaString,
    val character: String,
    val isCorrect: Boolean
)