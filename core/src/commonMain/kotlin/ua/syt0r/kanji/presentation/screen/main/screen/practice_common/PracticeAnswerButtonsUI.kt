package ua.syt0r.kanji.presentation.screen.main.screen.practice_common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.srs.SrsAnswer
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import kotlin.time.Duration

data class PracticeAnswers(
    val again: PracticeAnswer,
    val hard: PracticeAnswer,
    val good: PracticeAnswer,
    val easy: PracticeAnswer
)

data class PracticeAnswer(
    val srsAnswer: SrsAnswer,
    val mistakes: Int = 0
)

@Composable
fun PracticeAnswerButtonsContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    Column(
        modifier = modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        HorizontalDivider()
        content()
    }

}

@Composable
fun PracticeAnswerButtonsRow(
    answers: PracticeAnswers,
    enableKeyboardControls: Boolean = true,
    onClick: (PracticeAnswer) -> Unit,
    modifier: Modifier = Modifier
) {

    val keyboardControlsModifier = if (enableKeyboardControls) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        Modifier.focusable()
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyUp) return@onKeyEvent false

                val srsCard = when (event.key) {
                    Key.One -> answers.again
                    Key.Two -> answers.hard
                    Key.Three -> answers.good
                    Key.Four -> answers.easy
                    else -> null
                }

                srsCard?.let { onClick(it); true } ?: false
            }
    } else {
        Modifier
    }

    PracticeAnswerButtonsContainer(
        modifier = modifier.then(keyboardControlsModifier)
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
                .widthIn(min = 400.dp)
                .padding(horizontal = 20.dp)
                .width(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Top
        ) {
            SrsAnswerButton(
                label = resolveString { commonPractice.againButton },
                interval = answers.again.srsAnswer.card.interval,
                onClick = { onClick(answers.again) },
                color = MaterialTheme.colorScheme.error
            )
            SrsAnswerButton(
                label = resolveString { commonPractice.hardButton },
                interval = answers.hard.srsAnswer.card.interval,
                onClick = { onClick(answers.hard) },
                color = MaterialTheme.extraColorScheme.due
            )
            SrsAnswerButton(
                label = resolveString { commonPractice.goodButton },
                interval = answers.good.srsAnswer.card.interval,
                onClick = { onClick(answers.good) },
                color = MaterialTheme.extraColorScheme.success
            )
            SrsAnswerButton(
                label = resolveString { commonPractice.easyButton },
                interval = answers.easy.srsAnswer.card.interval,
                onClick = { onClick(answers.easy) },
                color = MaterialTheme.extraColorScheme.new
            )
        }
    }

}

data class ExpandableVocabPracticeAnswersRowState(
    val answers: PracticeAnswers,
    val showButton: Boolean
)

@Composable
fun ExpandablePracticeAnswerButtonsRow(
    state: State<ExpandableVocabPracticeAnswersRowState>,
    onClick: (PracticeAnswer) -> Unit,
    modifier: Modifier = Modifier,
) {

    AnimatedContent(
        targetState = state.value,
        contentKey = {},
        transitionSpec = { fadeIn(snap()) togetherWith fadeOut(snap()) },
        modifier = modifier
    ) { data ->

        val offset = animateFloatAsState(if (data.showButton) 0f else 1f)

        PracticeAnswerButtonsRow(
            answers = data.answers,
            onClick = onClick,
            enableKeyboardControls = data.showButton,
            modifier = Modifier.graphicsLayer { translationY = size.height * offset.value },
        )

    }

}

@Composable
fun FlashcardPracticeAnswerButtonsRow(
    answers: PracticeAnswers,
    showAnswer: State<Boolean>,
    onRevealAnswerClick: () -> Unit,
    onAnswerClick: (PracticeAnswer) -> Unit
) {

    Box(
        modifier = Modifier.height(IntrinsicSize.Max)
    ) {

        val hiddenButton = @Composable { isVisible: Boolean ->
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            SrsWholeRowButton(
                text = resolveString { commonPractice.flashcardRevealButton },
                onClick = onRevealAnswerClick,
                modifier = Modifier
                    .graphicsLayer { if (!isVisible) alpha = 0f }
                    .focusable()
                    .focusRequester(focusRequester)
                    .onKeyEvent {
                        if (it.type == KeyEventType.KeyUp && it.key == Key.Spacebar) {
                            onRevealAnswerClick()
                            true
                        } else false
                    }
            )

        }

        val revealedButton = @Composable { isVisible: Boolean ->
            PracticeAnswerButtonsRow(
                answers = answers,
                enableKeyboardControls = isVisible,
                onClick = { if (isVisible) onAnswerClick(it) },
                modifier = Modifier.graphicsLayer { if (!isVisible) alpha = 0f }
            )
        }

        // Laying out both for static button size
        when (showAnswer.value) {
            false -> {
                revealedButton(false)
                hiddenButton(true)
            }

            true -> {
                hiddenButton(false)
                revealedButton(true)
            }
        }

    }
}

expect val SrsAnswerButtonTextBlurRadius: Float

@Composable
fun RowScope.SrsAnswerButton(
    label: String,
    interval: Duration,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier
            .weight(1f)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = resolveString { commonPractice.formattedSrsInterval(interval) },
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                shadow = Shadow(color = color, blurRadius = SrsAnswerButtonTextBlurRadius)
            ),
            color = color
        )
    }

}

@Composable
private fun SrsWholeRowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    PracticeAnswerButtonsContainer(modifier = modifier) {

        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentWidth()
                .width(400.dp)
                .padding(horizontal = 20.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .wrapContentSize()
        )

    }

}
