package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.user_data.preferences.PreferencesVocabReadingPriority
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueueProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryItem
import kotlin.time.Duration

@Serializable
data class VocabPracticeScreenConfiguration(
    val wordIdToDeckIdMap: Map<Long, Long>,
    val practiceType: ScreenVocabPracticeType
)

enum class VocabPracticeReadingPriority(
    override val titleResolver: StringResolveScope<String>,
    val repoType: PreferencesVocabReadingPriority
) : DisplayableEnum {

    Default(
        titleResolver = { vocabPractice.readingPriorityConfigurationDefault },
        repoType = PreferencesVocabReadingPriority.Default
    ),
    Kanji(
        titleResolver = { vocabPractice.readingPriorityConfigurationKanji },
        repoType = PreferencesVocabReadingPriority.Kanji
    ),
    Kana(
        titleResolver = { vocabPractice.readingPriorityConfigurationKana },
        repoType = PreferencesVocabReadingPriority.Kana
    );

}

fun PreferencesVocabReadingPriority.toScreenType(): VocabPracticeReadingPriority {
    return VocabPracticeReadingPriority.values().first { it.repoType == this }
}

sealed interface VocabPracticeConfiguration {

    data class Flashcard(
        val translationInFront: MutableState<Boolean>
    ) : VocabPracticeConfiguration

    data class ReadingPicker(
        val showMeaning: MutableState<Boolean>
    ) : VocabPracticeConfiguration

}

sealed interface VocabReviewState {

    val word: JapaneseWord

    interface Flashcard : VocabReviewState {
        val reading: FuriganaString
        val noFuriganaReading: FuriganaString
        val meaning: String
        val showMeaningInFront: Boolean
        val showAnswer: State<Boolean>
    }

    interface Reading : VocabReviewState {
        val questionCharacter: String
        val showMeaning: Boolean
        val displayReading: State<FuriganaString>
        val answers: List<String>
        val correctAnswer: String
        val selectedAnswer: State<SelectedReadingAnswer?>
    }

    interface Writing : VocabReviewState {
        val charactersData: List<VocabCharacterWritingData>
        val selected: MutableState<VocabCharacterWritingData>
    }

}

sealed interface VocabCharacterWritingData {

    val character: String

    data class NoStrokes(
        override val character: String
    ) : VocabCharacterWritingData

    data class WithStrokes(
        override val character: String,
        val writerState: CharacterWriterState
    ) : VocabCharacterWritingData

}

data class SelectedReadingAnswer(
    val selected: String,
    val correct: String
) {
    val isCorrect = selected == correct
}


data class VocabPracticeReviewState(
    val progress: PracticeQueueProgress,
    val reviewState: VocabReviewState,
    val answers: PracticeAnswers
)

data class VocabSummaryItem(
    val word: JapaneseWord,
    val reading: FuriganaString,
    override val nextInterval: Duration
) : PracticeSummaryItem
