package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.flow.SharedFlow
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.core.user_data.model.WritingInputMethod
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeProgress

data class WritingScreenConfiguration(
    val characters: List<String>,
    val shuffle: Boolean,
    val hintMode: WritingPracticeHintMode,
    val inputMode: WritingPracticeInputMode,
    val useRomajiForKanaWords: Boolean,
    val noTranslationsLayout: Boolean,
    val leftHandedMode: Boolean,
    val altStrokeEvaluatorEnabled: Boolean,
)

data class WritingScreenLayoutConfiguration(
    val noTranslationsLayout: Boolean,
    val radicalsHighlight: State<Boolean>,
    val kanaAutoPlay: State<Boolean>,
    val leftHandedMode: Boolean
)

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
    val inputMethod: WritingInputMethod
) : DisplayableEnum {
    Stroke(
        titleResolver = { writingPractice.inputModeStroke },
        inputMethod = WritingInputMethod.Stroke
    ),
    Character(
        titleResolver = { writingPractice.inputModeCharacter },
        inputMethod = WritingInputMethod.Character
    )
}

fun WritingInputMethod.toInputMode(): WritingPracticeInputMode = WritingPracticeInputMode.values()
    .first { it.inputMethod == this }

enum class ReviewUserAction {
    StudyNext,
    Next,
    Repeat
}

data class MultipleStrokesInputData(
    val characterStrokes: List<Path>,
    val inputStrokes: List<Path>
)

data class SingleStrokeInputData(
    val userPath: Path,
    val kanjiPath: Path
)

sealed interface StrokeProcessingResult {

    data class Correct(
        val userPath: Path,
        val kanjiPath: Path
    ) : StrokeProcessingResult

    data class Mistake(
        val hintStroke: Path
    ) : StrokeProcessingResult

}


sealed interface WritingReviewState {

    val practiceProgress: PracticeProgress
    val characterDetails: WritingReviewCharacterDetails

    data class SingleStrokeInput(
        override val practiceProgress: PracticeProgress,
        override val characterDetails: WritingReviewCharacterDetails,
        val isStudyMode: Boolean,
        val drawnStrokesCount: State<Int>,
        val currentStrokeMistakes: State<Int>,
        val currentCharacterMistakes: State<Int>,
        val inputProcessingResults: SharedFlow<StrokeProcessingResult>
    ) : WritingReviewState

    data class MultipleStrokeInput(
        override val practiceProgress: PracticeProgress,
        override val characterDetails: WritingReviewCharacterDetails,
        val inputStrokes: MutableState<List<Path>>,
        val inputState: State<MultipleStrokeInputState>
    ) : WritingReviewState

}

sealed class WritingReviewCharacterDetails {

    abstract val character: String
    abstract val strokes: List<Path>
    abstract val words: List<JapaneseWord>
    abstract val encodedWords: List<JapaneseWord>

    data class KanaReviewDetails(
        override val character: String,
        override val strokes: List<Path>,
        override val words: List<JapaneseWord>,
        override val encodedWords: List<JapaneseWord>,
        val kanaSystem: CharacterClassification.Kana,
        val reading: KanaReading
    ) : WritingReviewCharacterDetails()

    data class KanjiReviewDetails(
        override val character: String,
        override val strokes: List<Path>,
        override val words: List<JapaneseWord>,
        override val encodedWords: List<JapaneseWord>,
        val radicals: List<CharacterRadical>,
        val on: List<String>,
        val kun: List<String>,
        val meanings: List<String>,
        val variants: String?
    ) : WritingReviewCharacterDetails()

}

sealed interface MultipleStrokeInputState {
    object Writing : MultipleStrokeInputState
    object Processing : MultipleStrokeInputState
    data class Processed(
        val results: List<StrokeProcessingResult>,
        val mistakes: Int
    ) : MultipleStrokeInputState
}

data class WritingReviewCharacterSummaryDetails(
    val strokesCount: Int,
    val isStudy: Boolean
)