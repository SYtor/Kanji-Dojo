package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.Help
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.kanji.AnimatedStroke
import ua.syt0r.kanji.presentation.common.ui.kanji.Kanji
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiBackground
import ua.syt0r.kanji.presentation.common.ui.kanji.Stroke
import ua.syt0r.kanji.presentation.common.ui.kanji.StrokeInput
import ua.syt0r.kanji.presentation.common.ui.kanji.defaultStrokeColor
import ua.syt0r.kanji.presentation.common.ui.kanji.rememberStrokeInputState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.MultipleStrokeInputState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.MultipleStrokesInputData
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ReviewUserAction
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.SingleStrokeInputData
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.StrokeProcessingResult
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingReviewState
import kotlin.math.max

private const val CharacterMistakesToRepeat = 3

private sealed interface AnswerButtonsState {
    object Hidden : AnswerButtonsState
    object StudyButtonShown : AnswerButtonsState
    data class Shown(
        val showNextButton: Boolean
    ) : AnswerButtonsState
}


private fun State<WritingReviewState>.toAnswerButtonsState(): State<AnswerButtonsState> {
    return derivedStateOf {
        when (val currentState = value) {

            is WritingReviewState.MultipleStrokeInput -> {
                val processed = currentState.inputState.value as? MultipleStrokeInputState.Processed
                if (processed == null) {
                    AnswerButtonsState.Hidden
                } else {
                    AnswerButtonsState.Shown(
                        showNextButton = processed.mistakes < CharacterMistakesToRepeat
                    )
                }
            }

            is WritingReviewState.SingleStrokeInput -> {
                val visible = currentState.run {
                    drawnStrokesCount.value == characterDetails.strokes.size
                }
                if (visible) {
                    if (currentState.isStudyMode) AnswerButtonsState.StudyButtonShown
                    else AnswerButtonsState.Shown(
                        showNextButton = currentState.currentCharacterMistakes.value < CharacterMistakesToRepeat
                    )
                } else {
                    AnswerButtonsState.Hidden
                }
            }

        }
    }
}

@Composable
fun WritingPracticeInputSection(
    state: State<WritingReviewState>,
    onSingleStrokeSubmit: (SingleStrokeInputData) -> Unit,
    onMultipleStokeSubmit: (MultipleStrokesInputData) -> Unit,
    onHintClick: () -> Unit,
    onNextClick: (ReviewUserAction) -> Unit,
    modifier: Modifier = Modifier
) {

    InputDecorations(
        modifier = modifier
    ) {

        val coroutineScope = rememberCoroutineScope()
        val hintClicksSharedFlow = remember { MutableSharedFlow<Unit>() }

        val transition = updateTransition(
            targetState = state.value,
            label = "Different Characters Transition"
        )

        transition.AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(),
                    initialContentExit = fadeOut()
                )
            }
        ) { data ->

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (data) {
                    is WritingReviewState.MultipleStrokeInput -> {
                        MultipleStrokeInputContent(
                            state = data,
                            submit = onMultipleStokeSubmit
                        )
                    }

                    is WritingReviewState.SingleStrokeInput -> {
                        SingleStrokeInputContent(
                            reviewState = data,
                            onStrokeDrawn = onSingleStrokeSubmit,
                            hintClicksFlow = hintClicksSharedFlow
                        )

                        val isHintButtonVisible = remember {
                            derivedStateOf {
                                data.run { drawnStrokesCount.value < characterDetails.strokes.size }
                            }
                        }

                        HintButton(
                            onHintClick = {
                                coroutineScope.launch {
                                    onHintClick()
                                    hintClicksSharedFlow.emit(Unit)
                                }
                            },
                            visible = isHintButtonVisible
                        )
                    }
                }
            }

        }

        AnswerButtons(
            answerButtonsState = state.toAnswerButtonsState(),
            onNextClick = onNextClick
        )

    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BoxScope.MultipleStrokeInputContent(
    state: WritingReviewState.MultipleStrokeInput,
    submit: (MultipleStrokesInputData) -> Unit
) {

    var strokes by state.inputStrokes

    when (val currentState = state.inputState.value) {

        is MultipleStrokeInputState.Processed -> {

            val lerpProgress = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                lerpProgress.animateTo(1f)
            }

            currentState.results.forEach { strokeResult ->
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

        else -> {

            Kanji(
                strokes = strokes,
                modifier = Modifier.fillMaxSize()
            )

        }

    }

    val transition = updateTransition(targetState = state.inputState.value)

    val inputEnabledState = remember {
        derivedStateOf { state.inputState.value == MultipleStrokeInputState.Writing }
    }

    if (inputEnabledState.value) {
        StrokeInput(
            onUserPathDrawn = { path -> strokes = strokes.plus(path) },
            modifier = Modifier.fillMaxSize()
        )
    }

    transition.AnimatedVisibility(
        visible = { it == MultipleStrokeInputState.Writing },
        modifier = Modifier.align(Alignment.BottomEnd)
    ) {
        IconButton(
            onClick = {
                submit(
                    MultipleStrokesInputData(
                        characterStrokes = state.characterDetails.strokes,
                        inputStrokes = strokes
                    )
                )
            }
        ) {
            Icon(Icons.Default.Check, null)
        }
    }

    transition.AnimatedVisibility(
        visible = { it == MultipleStrokeInputState.Writing },
        modifier = Modifier.align(Alignment.BottomStart)
    ) {
        IconButton(
            onClick = { strokes = strokes.dropLast(1) }
        ) {
            Icon(Icons.Default.Undo, null)
        }
    }

}

@Composable
private fun SingleStrokeInputContent(
    reviewState: WritingReviewState.SingleStrokeInput,
    onStrokeDrawn: (SingleStrokeInputData) -> Unit,
    hintClicksFlow: Flow<Unit>
) {

    val isAnimatingCorrectStroke = remember { mutableStateOf(false) }
    val correctStrokeAnimations = remember { Channel<StrokeProcessingResult.Correct>() }
    val inputState = rememberStrokeInputState(keepLastDrawnStroke = true)

    val mistakeStrokeAnimations = remember { Channel<StrokeProcessingResult.Mistake>() }

    val adjustedDrawnStrokesCount = remember {
        derivedStateOf {
            max(
                a = 0,
                b = reviewState.drawnStrokesCount.value - if (isAnimatingCorrectStroke.value) 1 else 0
            )
        }
    }

    Kanji(
        strokes = reviewState.characterDetails.strokes.take(adjustedDrawnStrokesCount.value),
        modifier = Modifier.fillMaxSize()
    )

    when (reviewState.isStudyMode) {
        true -> {
            StudyStroke(
                strokes = reviewState.characterDetails.strokes,
                drawnStrokesCount = adjustedDrawnStrokesCount,
                hintClicksFlow = hintClicksFlow
            )
        }

        false -> {
            HintStroke(
                reviewState = reviewState,
                hintClicksFlow = hintClicksFlow
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
        derivedStateOf { reviewState.characterDetails.strokes.size > reviewState.drawnStrokesCount.value }
    }

    LaunchedEffect(Unit) {
        reviewState.inputProcessingResults.collect {
            inputState.hideStroke()
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
                    SingleStrokeInputData(
                        userPath = drawnPath,
                        kanjiPath = reviewState.characterDetails.strokes[reviewState.drawnStrokesCount.value]
                    )
                )

            },
            state = inputState,
            modifier = Modifier.fillMaxSize()
        )
    }

}

@Composable
private fun BoxScope.HintButton(
    onHintClick: () -> Unit,
    visible: State<Boolean>
) {

    AnimatedVisibility(
        visible = visible.value,
        modifier = Modifier.align(Alignment.TopEnd)
    ) {
        IconButton(
            onClick = onHintClick
        ) {
            Icon(ExtraIcons.Help, null)
        }
    }

}


@Composable
private fun BoxScope.AnswerButtons(
    answerButtonsState: State<AnswerButtonsState>,
    onNextClick: (ReviewUserAction) -> Unit
) {

    val buttonsTransition = updateTransition(answerButtonsState.value)
    buttonsTransition.AnimatedContent(
        transitionSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn() togetherWith
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) + fadeOut()
        },
        contentKey = { it !is AnswerButtonsState.Hidden },
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {

            when (it) {
                AnswerButtonsState.Hidden -> {

                }

                AnswerButtonsState.StudyButtonShown -> {
                    StyledTextButton(
                        text = resolveString { writingPractice.studyFinishedButton },
                        icon = Icons.Default.KeyboardArrowRight,
                        contentColor = MaterialTheme.colorScheme.surfaceVariant,
                        backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = { onNextClick(ReviewUserAction.StudyNext) }
                    )
                }

                is AnswerButtonsState.Shown -> {
                    StyledTextButton(
                        text = resolveString { writingPractice.repeatButton },
                        icon = Icons.Default.Refresh,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        onClick = { onNextClick(ReviewUserAction.Repeat) }
                    )

                    if (it.showNextButton) {
                        Spacer(modifier = Modifier.width(16.dp))
                        StyledTextButton(
                            text = resolveString { writingPractice.nextButton },
                            icon = Icons.Default.KeyboardArrowRight,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            backgroundColor = MaterialTheme.extraColorScheme.success,
                            onClick = { onNextClick(ReviewUserAction.Next) }
                        )
                    }

                }
            }
        }
    }
}

@Composable
private fun StyledTextButton(
    text: String,
    icon: ImageVector,
    contentColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .padding(start = 16.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = contentColor
        )
        Spacer(modifier = Modifier.width(0.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor
        )
    }

}

@Composable
private fun InputDecorations(
    modifier: Modifier,
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
    }
}

@Composable
fun HintStroke(
    reviewState: WritingReviewState.SingleStrokeInput,
    hintClicksFlow: Flow<Unit>
) {

    val currentState by rememberUpdatedState(reviewState)

    val stroke = remember { mutableStateOf<Path?>(null, neverEqualPolicy()) }
    val strokeDrawProgress = remember { Animatable(initialValue = 0f) }
    val strokeAlpha = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(Unit) {

        hintClicksFlow.collectLatest {
            stroke.value = currentState.run {
                characterDetails.strokes.getOrNull(drawnStrokesCount.value)
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
