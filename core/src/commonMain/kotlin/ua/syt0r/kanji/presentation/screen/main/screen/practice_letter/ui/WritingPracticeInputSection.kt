package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterDecorations
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWritingProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswerButtonsRow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState

private sealed interface LetterWritingButtonsState {
    object Hidden : LetterWritingButtonsState
    object StudyButtons : LetterWritingButtonsState
    data class DefaultButtons(val answers: PracticeAnswers) : LetterWritingButtonsState
}

@Composable
private fun State<LetterPracticeReviewState.Writing>.toAnswerButtonsState(): State<LetterWritingButtonsState> =
    remember {
        derivedStateOf {
            val currentState = value
            val writerState = currentState.writerState.value
            val progress = writerState.progress.value

            when {
                progress is CharacterWritingProgress.Completed.Idle -> {
                    if (currentState.isStudyMode.value) LetterWritingButtonsState.StudyButtons
                    else LetterWritingButtonsState.DefaultButtons(currentState.answers)
                }

                else -> LetterWritingButtonsState.Hidden
            }
        }
    }

@Composable
fun WritingPracticeInputSection(
    state: State<LetterPracticeReviewState.Writing>,
    onNextClick: (PracticeAnswer) -> Unit,
    modifier: Modifier = Modifier
) {

    val writerState = remember { derivedStateOf { state.value.writerState.value } }

    CharacterWriterDecorations(
        modifier = modifier,
        state = writerState
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
                state = writerState.value,
                modifier = Modifier.fillMaxSize()
            )

        }

        AnswerButtons(
            letterWritingButtonsState = state.toAnswerButtonsState(),
            studyCompleted = { state.value.isStudyMode.value = false },
            answerSelected = onNextClick
        )

    }

}

@Composable
private fun BoxScope.AnswerButtons(
    letterWritingButtonsState: State<LetterWritingButtonsState>,
    studyCompleted: () -> Unit,
    answerSelected: (PracticeAnswer) -> Unit
) {

    val buttonsTransition = updateTransition(letterWritingButtonsState.value)
    buttonsTransition.AnimatedContent(
        transitionSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn() togetherWith
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) + fadeOut()
        },
        contentKey = { it !is LetterWritingButtonsState.Hidden },
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {

            when (it) {
                LetterWritingButtonsState.Hidden -> {

                }

                LetterWritingButtonsState.StudyButtons -> {
                    StyledTextButton(
                        text = resolveString { writingPractice.studyFinishedButton },
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentColor = MaterialTheme.colorScheme.surfaceVariant,
                        backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = studyCompleted,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                is LetterWritingButtonsState.DefaultButtons -> {
                    PracticeAnswerButtonsRow(
                        answers = it.answers,
                        onClick = answerSelected,
                        contentModifier = Modifier.padding(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
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
    onClick: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
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
