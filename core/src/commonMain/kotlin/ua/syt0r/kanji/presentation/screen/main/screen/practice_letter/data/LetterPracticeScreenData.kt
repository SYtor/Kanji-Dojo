package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Path
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterPracticeWritingInputMode
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationItemsSelectorState

@Serializable
data class LetterPracticeScreenConfiguration(
    val characterToDeckIdMap: Map<String, Long>,
    val practiceType: ScreenLetterPracticeType
)

sealed interface LetterPracticeConfiguration {

    data class Writing(
        val selectorState: PracticeConfigurationItemsSelectorState<String>,
        val hintMode: MutableState<WritingPracticeHintMode>,
        val inputMode: MutableState<WritingPracticeInputMode>,
        val useRomajiForKanaWords: MutableState<Boolean>,
        val noTranslationsLayout: MutableState<Boolean>,
        val leftHandedMode: MutableState<Boolean>,
        val altStrokeEvaluatorEnabled: MutableState<Boolean>,
    ) : LetterPracticeConfiguration

    data class Reading(
        val selectorState: PracticeConfigurationItemsSelectorState<String>,
        val useRomajiForKanaWords: MutableState<Boolean>
    ) : LetterPracticeConfiguration

}

sealed interface LetterPracticeLayoutConfiguration {

    val kanaAutoPlay: MutableState<Boolean>

    data class WritingLayoutConfiguration(
        val noTranslationsLayout: Boolean,
        val radicalsHighlight: MutableState<Boolean>,
        override val kanaAutoPlay: MutableState<Boolean>,
        val leftHandedMode: Boolean
    ) : LetterPracticeLayoutConfiguration

    data class ReadingLayoutConfiguration(
        override val kanaAutoPlay: MutableState<Boolean>
    ) : LetterPracticeLayoutConfiguration

}


sealed interface LetterPracticeReviewState {

    val layout: LetterPracticeLayoutConfiguration
    val itemData: LetterPracticeItemData

    data class Writing(
        override val layout: LetterPracticeLayoutConfiguration.WritingLayoutConfiguration,
        override val itemData: LetterPracticeItemData.WritingData,
        val answers: PracticeAnswers,
        val studyWriterState: CharacterWriterState?,
        val reviewWriterState: CharacterWriterState
    ) : LetterPracticeReviewState {
        val isStudyMode = mutableStateOf(value = studyWriterState != null)
        val writerState: State<CharacterWriterState> = derivedStateOf {
            if (isStudyMode.value) studyWriterState!! else reviewWriterState
        }
    }

    data class Reading(
        override val layout: LetterPracticeLayoutConfiguration.ReadingLayoutConfiguration,
        override val itemData: LetterPracticeItemData.ReadingData,
        val answers: PracticeAnswers,
        val revealed: MutableState<Boolean> = mutableStateOf(false)
    ) : LetterPracticeReviewState

}

enum class WritingPracticeHintMode(
    override val titleResolver: StringResolveScope<String>,
) : DisplayableEnum {
    OnlyNew(
        titleResolver = { writingPractice.hintStrokeNewOnlyMode }
    ),
    All(
        titleResolver = { writingPractice.hintStrokeAllMode }
    ),
    None(
        titleResolver = { writingPractice.hintStrokeNoneMode }
    )
}

enum class WritingPracticeInputMode(
    override val titleResolver: StringResolveScope<String>,
    val repoType: PreferencesLetterPracticeWritingInputMode
) : DisplayableEnum {
    Stroke(
        titleResolver = { writingPractice.inputModeStroke },
        repoType = PreferencesLetterPracticeWritingInputMode.Stroke
    ),
    Character(
        titleResolver = { writingPractice.inputModeCharacter },
        repoType = PreferencesLetterPracticeWritingInputMode.Character
    )
}

fun PreferencesLetterPracticeWritingInputMode.toScreenType(): WritingPracticeInputMode =
    WritingPracticeInputMode.values()
        .first { it.repoType == this }

interface LetterPracticeItemData {

    interface KanaData : LetterPracticeItemData {
        val kanaSystem: CharacterClassification.Kana
        val reading: KanaReading
    }

    interface KanjiData : LetterPracticeItemData {
        val radicals: List<CharacterRadical>
        val on: List<String>
        val kun: List<String>
        val meanings: List<String>
        val variants: String?
    }

    sealed interface WritingData : LetterPracticeItemData {
        val strokes: List<Path>
    }

    sealed interface ReadingData : LetterPracticeItemData

    val character: String
    val words: List<JapaneseWord>
    val encodedWords: List<JapaneseWord>

    data class KanaWritingData(
        override val character: String,
        override val strokes: List<Path>,
        override val words: List<JapaneseWord>,
        override val encodedWords: List<JapaneseWord>,
        override val kanaSystem: CharacterClassification.Kana,
        override val reading: KanaReading
    ) : WritingData, KanaData

    data class KanaReadingData(
        override val character: String,
        override val words: List<JapaneseWord>,
        override val encodedWords: List<JapaneseWord>,
        override val kanaSystem: CharacterClassification.Kana,
        override val reading: KanaReading
    ) : ReadingData, KanaData

    data class KanjiWritingData(
        override val character: String,
        override val strokes: List<Path>,
        override val words: List<JapaneseWord>,
        override val encodedWords: List<JapaneseWord>,
        override val radicals: List<CharacterRadical>,
        override val on: List<String>,
        override val kun: List<String>,
        override val meanings: List<String>,
        override val variants: String?
    ) : WritingData, KanjiData

    data class KanjiReadingData(
        override val character: String,
        override val words: List<JapaneseWord>,
        override val encodedWords: List<JapaneseWord>,
        override val radicals: List<CharacterRadical>,
        override val on: List<String>,
        override val kun: List<String>,
        override val meanings: List<String>,
        override val variants: String?
    ) : ReadingData, KanjiData

}
