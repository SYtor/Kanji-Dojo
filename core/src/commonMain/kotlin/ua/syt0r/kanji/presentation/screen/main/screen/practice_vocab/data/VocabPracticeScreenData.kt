package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.srs.SrsCard
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.user_data.preferences.PreferencesVocabPracticeType
import ua.syt0r.kanji.core.user_data.preferences.VocabReadingPriority
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum

enum class VocabPracticeType(
    val preferencesType: PreferencesVocabPracticeType,
    override val titleResolver: StringResolveScope<String>
) : DisplayableEnum {

    ReadingPicker(
        preferencesType = PreferencesVocabPracticeType.ReadingPicker,
        titleResolver = { vocabPractice.practiceTypeReadingPicker }
    ),
    Flashcard(
        preferencesType = PreferencesVocabPracticeType.Flashcard,
        titleResolver = { vocabPractice.practiceTypeFlashcard }
    ),
    Writing(
        preferencesType = PreferencesVocabPracticeType.Writing,
        titleResolver = { vocabPractice.practiceTypeWriting }
    );

    companion object {
        fun from(practiceType: PreferencesVocabPracticeType): VocabPracticeType {
            return values().first { it.preferencesType == practiceType }
        }
    }

}

fun VocabPracticeType.toSrsItemKey(wordId: Long): SrsCardKey {
    return SrsCardKey(
        itemKey = wordId.toString(),
        practiceType = vocabPracticeTypeToSrsPracticeTypeMapping.getValue(this)
    )
}

private val vocabPracticeTypeToSrsPracticeTypeMapping = mapOf(
    VocabPracticeType.Flashcard to "flashcard",
    VocabPracticeType.ReadingPicker to "read_pick",
    VocabPracticeType.Writing to "writing",
)

enum class VocabPracticeReadingPriority(
    override val titleResolver: StringResolveScope<String>,
    val repoType: VocabReadingPriority
) : DisplayableEnum {

    Default(
        titleResolver = { vocabPractice.readingPriorityConfigurationDefault },
        repoType = VocabReadingPriority.Default
    ),
    Kanji(
        titleResolver = { vocabPractice.readingPriorityConfigurationKanji },
        repoType = VocabReadingPriority.Kanji
    ),
    Kana(
        titleResolver = { vocabPractice.readingPriorityConfigurationKana },
        repoType = VocabReadingPriority.Kana
    );

}

fun VocabReadingPriority.toScreenType(): VocabPracticeReadingPriority {
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

data class VocabPracticeSrsAnswers(
    val again: SrsCard,
    val hard: SrsCard,
    val good: SrsCard,
    val easy: SrsCard
)

data class VocabPracticeReviewState(
    val progress: VocabQueueProgress,
    val reviewState: VocabReviewState,
    val answers: VocabPracticeSrsAnswers
)

data class VocabSummaryItem(
    val word: JapaneseWord,
    val reading: FuriganaString,
    val nextReview: Instant
)
