package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterInputState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterDecorations
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.MultipleStrokeInputContentState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ReviewUserAction
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingReviewState

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
        val writerState = value.writerState
        when (val writerInputState = writerState.inputState) {
            is CharacterInputState.MultipleStroke -> {
                val processed = writerInputState.contentState
                    .value as? MultipleStrokeInputContentState.Processed
                if (processed == null) {
                    AnswerButtonsState.Hidden
                } else {
                    AnswerButtonsState.Shown(
                        showNextButton = processed.mistakes < CharacterMistakesToRepeat
                    )
                }
            }

            is CharacterInputState.SingleStroke -> {
                val visible = writerInputState.run {
                    drawnStrokesCount.value == writerState.strokes.size
                }
                if (visible) {
                    if (writerInputState.isStudyMode) AnswerButtonsState.StudyButtonShown
                    else AnswerButtonsState.Shown(
                        showNextButton = writerInputState.totalMistakes.value < CharacterMistakesToRepeat
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
    onNextClick: (ReviewUserAction) -> Unit,
    modifier: Modifier = Modifier
) {

    CharacterWriterDecorations(
        modifier = modifier
    ) {

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

            CharacterWriter(
                state = data.writerState,
                modifier = Modifier.fillMaxSize()
            )

        }

        AnswerButtons(
            answerButtonsState = state.toAnswerButtonsState(),
            onNextClick = onNextClick
        )

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
