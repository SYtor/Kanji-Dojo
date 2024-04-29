package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.data

import androidx.compose.ui.graphics.Path


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

    data class Mistake(val hintStroke: Path) : StrokeProcessingResult

}
