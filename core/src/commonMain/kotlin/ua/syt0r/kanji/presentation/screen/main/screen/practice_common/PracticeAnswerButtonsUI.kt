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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.srs.SrsAnswer
import ua.syt0r.kanji.core.theme_manager.LocalThemeManager
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
    val srsAnswer: SrsAnswer
)

@Composable
fun PracticeAnswerButtonsRow(
    answers: PracticeAnswers,
    onClick: (PracticeAnswer) -> Unit,
    enableKeyboardControls: Boolean = true,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
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

    val theme = LocalThemeManager.current
    val rowThemeModifier = when {
        theme.isDarkTheme -> Modifier.clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp)

        else -> Modifier.shadow(2.dp, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
    }

    val buttonThemeModifier = when {
        theme.isDarkTheme -> Modifier.clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)

        else -> Modifier
    }

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState())
            .then(contentModifier)
            .then(rowThemeModifier)
            .then(keyboardControlsModifier)
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(
            space = if (theme.isDarkTheme) 4.dp else 2.dp
        )
    ) {
        SrsAnswerButton(
            label = resolveString { vocabPractice.againButton },
            interval = answers.again.srsAnswer.card.interval,
            onClick = { onClick(answers.again) },
            color = MaterialTheme.colorScheme.error,
            outerModifier = buttonThemeModifier,
            innerModifier = Modifier.padding(start = 2.dp)
        )
        SrsAnswerButton(
            label = resolveString { vocabPractice.hardButton },
            interval = answers.hard.srsAnswer.card.interval,
            onClick = { onClick(answers.hard) },
            color = MaterialTheme.extraColorScheme.due,
            outerModifier = buttonThemeModifier
        )
        SrsAnswerButton(
            label = resolveString { vocabPractice.goodButton },
            interval = answers.good.srsAnswer.card.interval,
            onClick = { onClick(answers.good) },
            color = MaterialTheme.extraColorScheme.success,
            outerModifier = buttonThemeModifier
        )
        SrsAnswerButton(
            label = resolveString { vocabPractice.easyButton },
            interval = answers.easy.srsAnswer.card.interval,
            onClick = { onClick(answers.easy) },
            color = MaterialTheme.extraColorScheme.new,
            outerModifier = buttonThemeModifier,
            innerModifier = Modifier.padding(end = 2.dp)
        )
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
            modifier = Modifier.fillMaxSize()
                .graphicsLayer { translationY = size.height * offset.value },
            contentModifier = Modifier.padding(20.dp)
        )

    }

}

@Composable
fun RowScope.SrsAnswerButton(
    label: String,
    interval: Duration,
    color: Color,
    onClick: () -> Unit,
    outerModifier: Modifier,
    innerModifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.weight(1f)
            .fillMaxHeight()
            .then(outerModifier)
            .clickable(onClick = onClick)
            .then(innerModifier)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = resolveString { vocabPractice.formattedSrsInterval(interval) },
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                shadow = Shadow(color = color, blurRadius = 1f)
            ),
            color = color
        )
    }

}