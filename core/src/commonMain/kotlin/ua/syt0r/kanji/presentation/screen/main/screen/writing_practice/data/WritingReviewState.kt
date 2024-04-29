package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.flow.SharedFlow
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeProgress

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