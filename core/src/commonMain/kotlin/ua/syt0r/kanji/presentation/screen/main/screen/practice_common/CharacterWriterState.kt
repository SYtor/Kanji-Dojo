package ua.syt0r.kanji.presentation.screen.main.screen.practice_common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.launchUnit
import ua.syt0r.kanji.core.stroke_evaluator.KanjiStrokeEvaluator
import kotlin.math.max


interface CharacterWriterState {

    val character: String
    val strokes: List<Path>
    val inputState: CharacterInputState
    val writingStatus: State<CharacterWritingStatus>

    fun submit(inputData: CharacterInputData)
    fun notifyHintClick()

}

sealed interface CharacterWriterConfiguration {

    data class StrokeInput(
        val isStudyMode: Boolean
    ) : CharacterWriterConfiguration

    object CharacterInput : CharacterWriterConfiguration

}

sealed interface CharacterInputData {

    data class MultipleStrokes(
        val characterStrokes: List<Path>,
        val inputStrokes: List<Path>
    ) : CharacterInputData

    data class SingleStroke(
        val userPath: Path,
        val kanjiPath: Path
    ) : CharacterInputData
}

sealed interface StrokeProcessingResult {

    data class Correct(
        val userPath: Path,
        val kanjiPath: Path
    ) : StrokeProcessingResult

    data class Mistake(
        val hintStroke: Path
    ) : StrokeProcessingResult

}

sealed interface CharacterInputState {

    interface SingleStroke : CharacterInputState {

        val isStudyMode: Boolean
        val drawnStrokesCount: State<Int>
        val currentStrokeMistakes: State<Int>
        val totalMistakes: State<Int>
        val hintClicksSharedFlow: SharedFlow<Unit>
        val inputProcessingResults: SharedFlow<StrokeProcessingResult>

        suspend fun notifyHintClick()

    }

    sealed interface MultipleStroke : CharacterInputState {
        val contentState: State<MultipleStrokeInputContentState>
    }

}

sealed interface MultipleStrokeInputContentState {

    data class Writing(
        val strokes: MutableState<List<Path>>
    ) : MultipleStrokeInputContentState

    data class Processing(
        val strokes: List<Path>
    ) : MultipleStrokeInputContentState

    data class Processed(
        val strokeProcessingResults: List<StrokeProcessingResult>,
        val mistakes: Int
    ) : MultipleStrokeInputContentState

}

private data class MutableSingleStrokeInputState(
    override val isStudyMode: Boolean,
    override val drawnStrokesCount: MutableState<Int>,
    override val currentStrokeMistakes: MutableState<Int>,
    override val totalMistakes: MutableState<Int>,
    override val hintClicksSharedFlow: MutableSharedFlow<Unit>,
    override val inputProcessingResults: MutableSharedFlow<StrokeProcessingResult>
) : CharacterInputState.SingleStroke {

    override suspend fun notifyHintClick() {
        hintClicksSharedFlow.emit(Unit)
    }

}

private data class MutableMultiStrokeInputState(
    override val contentState: MutableState<MultipleStrokeInputContentState>
) : CharacterInputState.MultipleStroke

sealed interface CharacterWritingStatus {
    object InProcess : CharacterWritingStatus
    data class Completed(val isCorrect: Boolean, val mistakes: Int) : CharacterWritingStatus
}

class DefaultCharacterWriterState(
    private val coroutineScope: CoroutineScope,
    private val strokeEvaluator: KanjiStrokeEvaluator,
    override val character: String,
    override val strokes: List<Path>,
    private val configuration: CharacterWriterConfiguration
) : CharacterWriterState {

    override val inputState: CharacterInputState = createInputState()

    override val writingStatus: State<CharacterWritingStatus> = derivedStateOf {
        when (inputState) {
            is CharacterInputState.MultipleStroke -> inputState.toWritingStatus()
            is CharacterInputState.SingleStroke -> inputState.toWritingStatus()
        }
    }

    override fun submit(inputData: CharacterInputData) = coroutineScope.launchUnit {
        when (inputData) {
            is CharacterInputData.SingleStroke -> {
                handleSingleStrokeInput(
                    inputData = inputData,
                    mutableState = inputState as MutableSingleStrokeInputState
                )
            }

            is CharacterInputData.MultipleStrokes -> {
                handleMultipleStrokeInput(
                    inputData = inputData,
                    inputState = inputState as MutableMultiStrokeInputState
                )
            }
        }
    }

    override fun notifyHintClick() {
        inputState as MutableSingleStrokeInputState
        inputState.currentStrokeMistakes.value++
        inputState.totalMistakes.value++
    }

    private fun createInputState(): CharacterInputState {
        return when (configuration) {
            is CharacterWriterConfiguration.StrokeInput -> {
                MutableSingleStrokeInputState(
                    isStudyMode = configuration.isStudyMode,
                    drawnStrokesCount = mutableStateOf(0),
                    currentStrokeMistakes = mutableStateOf(0),
                    totalMistakes = mutableStateOf(0),
                    hintClicksSharedFlow = MutableSharedFlow(),
                    inputProcessingResults = MutableSharedFlow()
                )
            }

            CharacterWriterConfiguration.CharacterInput -> {
                MutableMultiStrokeInputState(
                    contentState = mutableStateOf(
                        MultipleStrokeInputContentState.Writing(strokes = mutableStateOf(emptyList()))
                    )
                )
            }
        }
    }

    private suspend fun handleSingleStrokeInput(
        inputData: CharacterInputData.SingleStroke,
        mutableState: MutableSingleStrokeInputState
    ) {
        val isDrawnCorrectly = withContext(Dispatchers.IO) {
            strokeEvaluator.areStrokesSimilar(inputData.kanjiPath, inputData.userPath)
        }
        val result = if (isDrawnCorrectly) {
            mutableState.drawnStrokesCount.value += 1
            StrokeProcessingResult.Correct(
                userPath = inputData.userPath,
                kanjiPath = inputData.kanjiPath
            )
        } else {
            val currentStrokeMistakes = mutableState.run {
                currentStrokeMistakes.value += 1
                totalMistakes.value += 1
                currentStrokeMistakes.value
            }
            val path = when {
                currentStrokeMistakes > 2 -> inputData.kanjiPath
                else -> inputData.userPath
            }
            StrokeProcessingResult.Mistake(path)
        }
        mutableState.inputProcessingResults.emit(result)
    }

    private suspend fun handleMultipleStrokeInput(
        inputData: CharacterInputData.MultipleStrokes,
        inputState: MutableMultiStrokeInputState
    ) {
        val contentState = inputState.contentState
        contentState.value = MultipleStrokeInputContentState.Processing(inputData.inputStrokes)

        val processedState = withContext(Dispatchers.IO) {
            val strokesCount = max(inputData.characterStrokes.size, inputData.inputStrokes.size)
            val results = (0 until strokesCount).map { index ->
                val input = inputData.inputStrokes.getOrNull(index)
                val stroke = inputData.characterStrokes.getOrNull(index)

                if (input == null || stroke == null) {
                    StrokeProcessingResult.Mistake(
                        hintStroke = input ?: stroke!!
                    )
                } else {
                    val similar = strokeEvaluator.areStrokesSimilar(stroke, input)
                    if (similar) {
                        StrokeProcessingResult.Correct(input, stroke)
                    } else {
                        StrokeProcessingResult.Mistake(hintStroke = stroke)
                    }
                }
            }

            MultipleStrokeInputContentState.Processed(
                strokeProcessingResults = results.take(inputData.characterStrokes.size),
                mistakes = results.count { it is StrokeProcessingResult.Mistake }
            )
        }

        contentState.value = processedState
    }

    private fun CharacterInputState.MultipleStroke.toWritingStatus(): CharacterWritingStatus {
        return when (val currentState = contentState.value) {
            is MultipleStrokeInputContentState.Writing,
            is MultipleStrokeInputContentState.Processing -> CharacterWritingStatus.InProcess

            is MultipleStrokeInputContentState.Processed -> CharacterWritingStatus.Completed(
                isCorrect = isResultCorrect(currentState.mistakes),
                mistakes = currentState.mistakes
            )
        }
    }

    private fun CharacterInputState.SingleStroke.toWritingStatus(): CharacterWritingStatus {
        return when (drawnStrokesCount.value == strokes.size) {
            false -> CharacterWritingStatus.InProcess
            true -> {
                val mistakes = totalMistakes.value
                CharacterWritingStatus.Completed(
                    isCorrect = isResultCorrect(mistakes),
                    mistakes = mistakes
                )
            }
        }
    }

    private fun isResultCorrect(characterMistakes: Int): Boolean {
        return when (strokes.size) {
            1 -> characterMistakes == 0
            2, 3 -> characterMistakes < 2
            else -> characterMistakes <= 2
        }
    }

}
