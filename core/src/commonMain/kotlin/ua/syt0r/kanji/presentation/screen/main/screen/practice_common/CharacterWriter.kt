package ua.syt0r.kanji.presentation.screen.main.screen.practice_common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.Help
import ua.syt0r.kanji.presentation.common.theme.snapToBiggerContainerCrossfadeTransitionSpec
import ua.syt0r.kanji.presentation.common.ui.kanji.AnimatedStroke
import ua.syt0r.kanji.presentation.common.ui.kanji.Kanji
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiBackground
import ua.syt0r.kanji.presentation.common.ui.kanji.Stroke
import ua.syt0r.kanji.presentation.common.ui.kanji.StrokeInput
import ua.syt0r.kanji.presentation.common.ui.kanji.defaultStrokeColor
import ua.syt0r.kanji.presentation.common.ui.kanji.rememberStrokeInputState
import kotlin.math.max

@Composable
fun CharacterWriter(
    state: CharacterWriterState,
    modifier: Modifier = Modifier
) {

    Box(modifier) {
        when (val inputState = state.inputState) {

            is CharacterInputState.SingleStroke -> {

                SingleStrokeInputContent(
                    strokes = state.strokes,
                    inputState = inputState,
                    onStrokeDrawn = { state.submit(it) }
                )

            }

            is CharacterInputState.MultipleStroke -> {
                MultipleStrokeInputContent(
                    state = inputState,
                )
            }
        }
    }

}

@Composable
private fun BoxScope.MultipleStrokeInputContent(
    state: CharacterInputState.MultipleStroke
) {

    val currentState = state.contentState.value

    when (currentState) {
        is MultipleStrokeInputContentState.Writing -> {
            var strokes by currentState.strokes

            Kanji(
                strokes = strokes,
                modifier = Modifier.fillMaxSize()
            )

            StrokeInput(
                onUserPathDrawn = { path -> strokes = strokes.plus(path) },
                modifier = Modifier.fillMaxSize()
            )
        }

        is MultipleStrokeInputContentState.Processing -> {
            Kanji(
                strokes = currentState.strokes,
                modifier = Modifier.fillMaxSize()
            )
        }

        is MultipleStrokeInputContentState.Processed -> {

            val lerpProgress = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                lerpProgress.animateTo(1f)
            }

            currentState.strokeProcessingResults.forEach { strokeResult ->
                when (strokeResult) {
                    is StrokeProcessingResult.Correct -> {
                        AnimatedStroke(
                            fromPath = strokeResult.userPath,
                            toPath = strokeResult.kanjiPath,
                            progress = { lerpProgress.value },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is StrokeProcessingResult.Mistake -> {
                        Stroke(
                            path = strokeResult.hintStroke,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

    }

}

@Composable
private fun SingleStrokeInputContent(
    strokes: List<Path>,
    inputState: CharacterInputState.SingleStroke,
    onStrokeDrawn: (CharacterInputData.SingleStroke) -> Unit
) {

    val isAnimatingCorrectStroke = remember { mutableStateOf(false) }
    val correctStrokeAnimations = remember { Channel<StrokeProcessingResult.Correct>() }
    val strokeInputState = rememberStrokeInputState(keepLastDrawnStroke = true)

    val mistakeStrokeAnimations = remember { Channel<StrokeProcessingResult.Mistake>() }

    val adjustedDrawnStrokesCount = remember {
        derivedStateOf {
            max(
                a = 0,
                b = inputState.drawnStrokesCount.value - if (isAnimatingCorrectStroke.value) 1 else 0
            )
        }
    }

    Kanji(
        strokes = strokes.take(adjustedDrawnStrokesCount.value),
        modifier = Modifier.fillMaxSize()
    )

    when (inputState.isStudyMode) {
        true -> {
            StudyStroke(
                strokes = strokes,
                drawnStrokesCount = adjustedDrawnStrokesCount,
                hintClicksFlow = inputState.hintClicksSharedFlow
            )
        }

        false -> {
            HintStroke(
                strokes = strokes,
                inputState = inputState,
                hintClicksFlow = inputState.hintClicksSharedFlow
            )
        }
    }

    ErrorFadeOutStroke(
        mistakeFlow = remember { mistakeStrokeAnimations.consumeAsFlow() },
        onAnimationEnd = { }
    )

    CorrectMovingStroke(
        correctFlow = remember { correctStrokeAnimations.consumeAsFlow() },
        onAnimationEnd = { isAnimatingCorrectStroke.value = false }
    )

    val shouldShowStrokeInput by remember {
        derivedStateOf { strokes.size > inputState.drawnStrokesCount.value }
    }

    LaunchedEffect(Unit) {
        inputState.inputProcessingResults.collect {
            strokeInputState.hideStroke()
            when (it) {
                is StrokeProcessingResult.Correct -> {
                    correctStrokeAnimations.trySend(it)
                    isAnimatingCorrectStroke.value = true
                }

                is StrokeProcessingResult.Mistake -> {
                    mistakeStrokeAnimations.trySend(it)
                }
            }
        }
    }

    if (shouldShowStrokeInput) {
        StrokeInput(
            onUserPathDrawn = { drawnPath ->
                onStrokeDrawn(
                    CharacterInputData.SingleStroke(
                        userPath = drawnPath,
                        kanjiPath = strokes[inputState.drawnStrokesCount.value]
                    )
                )

            },
            state = strokeInputState,
            modifier = Modifier.fillMaxSize()
        )
    }

}

@Composable
fun CharacterWriterDecorations(
    modifier: Modifier,
    state: State<CharacterWriterState?>,
    content: @Composable BoxScope.() -> Unit
) {
    val inputShape = MaterialTheme.shapes.extraLarge
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = inputShape
            )
            .clip(inputShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = inputShape
            )
    ) {

        KanjiBackground(Modifier.fillMaxSize())

        content()

        val hintButtonTransition = updateTransition(
            targetState = derivedStateOf {
                val writerState = state.value
                val inputState = writerState?.inputState
                    ?.let { it as? CharacterInputState.SingleStroke }
                val writingStatus = writerState?.writingStatus?.value

                inputState?.takeIf { writingStatus == CharacterWritingStatus.InProcess }
            }.value
        )

        hintButtonTransition.AnimatedContent(
            transitionSpec = snapToBiggerContainerCrossfadeTransitionSpec(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            if (it == null) return@AnimatedContent

            val coroutineScope = rememberCoroutineScope()
            IconButton(
                onClick = {
                    coroutineScope.launch { it.notifyHintClick() }
                }
            ) {
                Icon(ExtraIcons.Help, null)
            }

        }

        val multipleStrokeButtonsTransition = updateTransition(
            targetState = derivedStateOf {
                val writerState = state.value
                val multipleStrokesContentState = writerState?.inputState
                    ?.let { it as? CharacterInputState.MultipleStroke }
                    ?.contentState
                    ?.value
                writerState to multipleStrokesContentState
            }.value
        )

        multipleStrokeButtonsTransition.AnimatedContent(
            contentKey = { (_, contentState) -> contentState },
            transitionSpec = snapToBiggerContainerCrossfadeTransitionSpec(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) { (writerState, contentState) ->
            if (contentState !is MultipleStrokeInputContentState.Writing) return@AnimatedContent

            IconButton(
                onClick = {
                    writerState!!.submit(
                        CharacterInputData.MultipleStrokes(
                            characterStrokes = writerState.strokes,
                            inputStrokes = contentState.strokes.value
                        )
                    )
                }
            ) {
                Icon(Icons.Default.Check, null)
            }
        }

        multipleStrokeButtonsTransition.AnimatedContent(
            contentKey = { (_, contentState) -> contentState },
            transitionSpec = snapToBiggerContainerCrossfadeTransitionSpec(),
            modifier = Modifier.align(Alignment.BottomStart)
        ) { (_, contentState) ->
            if (contentState !is MultipleStrokeInputContentState.Writing) {
                Box(Modifier)
                return@AnimatedContent
            }

            IconButton(
                onClick = {
                    contentState.strokes.value = contentState.strokes.value.dropLast(1)
                }
            ) {
                Icon(Icons.Default.Undo, null)
            }
        }

    }
}

@Composable
fun HintStroke(
    strokes: List<Path>,
    inputState: CharacterInputState.SingleStroke,
    hintClicksFlow: Flow<Unit>
) {

    val currentState by rememberUpdatedState(inputState)

    val stroke = remember { mutableStateOf<Path?>(null, neverEqualPolicy()) }
    val strokeDrawProgress = remember { Animatable(initialValue = 0f) }
    val strokeAlpha = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(Unit) {

        hintClicksFlow.collectLatest {
            stroke.value = currentState.run {
                strokes.getOrNull(drawnStrokesCount.value)
            }

            strokeAlpha.snapTo(1f)
            strokeDrawProgress.snapTo(0f)

            strokeDrawProgress.animateTo(1f, tween(600))
            strokeAlpha.animateTo(0f)

            stroke.value = null
        }

    }

    stroke.value?.let {
        AnimatedStroke(
            stroke = it,
            modifier = Modifier.fillMaxSize(),
            strokeColor = MaterialTheme.colorScheme.error,
            drawProgress = { strokeDrawProgress.value },
            strokeAlpha = { strokeAlpha.value }
        )
    }

}

@Composable
fun ErrorFadeOutStroke(
    mistakeFlow: Flow<StrokeProcessingResult.Mistake>,
    onAnimationEnd: () -> Unit
) {

    val lastData = remember { mutableStateOf<StrokeProcessingResult.Mistake?>(null) }
    val strokeAlpha = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(Unit) {
        mistakeFlow.collect {
            lastData.value = it
            strokeAlpha.snapTo(1f)
            strokeAlpha.animateTo(0f, tween(600))
            onAnimationEnd()
        }
    }

    lastData.value?.let {
        AnimatedStroke(
            stroke = it.hintStroke,
            modifier = Modifier.fillMaxSize(),
            strokeColor = MaterialTheme.colorScheme.error,
            drawProgress = { 1f },
            strokeAlpha = { strokeAlpha.value }
        )
    }

}

@Composable
fun CorrectMovingStroke(
    correctFlow: Flow<StrokeProcessingResult.Correct>,
    onAnimationEnd: () -> Unit
) {

    val lastData = remember { mutableStateOf<StrokeProcessingResult.Correct?>(null) }
    val strokeLength = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(Unit) {
        correctFlow.collect {
            lastData.value = it
            strokeLength.snapTo(0f)
            strokeLength.animateTo(1f)
            lastData.value = null
            onAnimationEnd()
        }
    }

    lastData.value?.let {
        AnimatedStroke(
            fromPath = it.userPath,
            toPath = it.kanjiPath,
            progress = { strokeLength.value },
            modifier = Modifier.fillMaxSize()
        )
    }

}

@Composable
private fun StudyStroke(
    strokes: List<Path>,
    drawnStrokesCount: State<Int>,
    hintClicksFlow: Flow<Unit>
) {

    val stroke = remember { mutableStateOf<Path?>(null) }
    val strokeDrawProgress = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(Unit) {
        val autoStartFlow = merge(flowOf(Unit), hintClicksFlow)
        snapshotFlow { drawnStrokesCount.value }
            .combine(autoStartFlow) { a, b -> a }
            .collectLatest { drawnStrokesCount ->
                // Waits for stroke animation to complete
                stroke.value = strokes.getOrNull(drawnStrokesCount)
                strokeDrawProgress.snapTo(0f)
                if (drawnStrokesCount == 0) delay(300)
                strokeDrawProgress.animateTo(1f, tween(600))
            }

    }

    stroke.value?.let {
        AnimatedStroke(
            stroke = it,
            modifier = Modifier.fillMaxSize(),
            strokeColor = defaultStrokeColor(),
            drawProgress = { strokeDrawProgress.value },
            strokeAlpha = { 0.5f }
        )
    }

}
