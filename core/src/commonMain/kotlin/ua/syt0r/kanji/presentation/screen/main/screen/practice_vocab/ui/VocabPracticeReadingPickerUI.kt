package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.neutralButtonColors
import ua.syt0r.kanji.presentation.common.theme.neutralTextButtonColors
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.SelectedReadingAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewState

@Composable
fun VocabPracticeReadingPickerUI(
    reviewState: VocabReviewState.Reading,
    onWordClick: (JapaneseWord) -> Unit,
    onAnswerSelected: (String) -> Unit,
    onNextClick: () -> Unit,
    onFeedbackClick: (JapaneseWord) -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {

        val selectedAnswer by reviewState.selectedAnswer

        FuriganaText(
            furiganaString = reviewState.displayReading.value,
            textStyle = MaterialTheme.typography.displayLarge,
            annotationTextStyle = MaterialTheme.typography.bodyLarge,
        )

        if (selectedAnswer != null || reviewState.showMeaning)
            TextButton(
                onClick = { onWordClick(reviewState.word) },
                colors = ButtonDefaults.neutralTextButtonColors(),
                modifier = Modifier.widthIn(max = 400.dp)
            ) {
                Text(reviewState.word.meanings.first())
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            }

        Spacer(modifier = Modifier.weight(1f))

        val maxItemsInEachRow = when (LocalOrientation.current) {
            Orientation.Portrait -> 2
            Orientation.Landscape -> 4
        }
        val answerRows = reviewState.answers.chunked(maxItemsInEachRow)

        Column(
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            answerRows.forEach { rowAnswers ->

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    rowAnswers.forEach { answer ->
                        ReadingAnswerButton(
                            answer = answer,
                            selectedAnswer = selectedAnswer,
                            enabled = selectedAnswer == null,
                            onClick = { onAnswerSelected(answer) },
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                        )

                    }

                    val lastLineItems = rowAnswers.size % maxItemsInEachRow
                    val missingSpots = maxItemsInEachRow - lastLineItems
                    if (lastLineItems != 0)
                        Spacer(Modifier.weight(missingSpots.toFloat()))

                }

            }
        }

        NextButton(
            showNextButton = remember(reviewState) {
                derivedStateOf { reviewState.selectedAnswer.value != null }
            },
            onClick = onNextClick,
            onFeedbackClick = { onFeedbackClick(reviewState.word) }
        )
    }
}


@Composable
private fun ReadingAnswerButton(
    answer: String,
    selectedAnswer: SelectedReadingAnswer?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val (textColor, containerColor) = MaterialTheme.run {
        when {
            answer == selectedAnswer?.correct -> {
                colorScheme.surface to extraColorScheme.success
            }

            answer == selectedAnswer?.selected && !selectedAnswer.isCorrect -> {
                colorScheme.surface to colorScheme.error
            }

            else -> {
                colorScheme.onSurface to colorScheme.surface
            }
        }
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            contentColor = textColor,
            disabledContentColor = textColor,
            containerColor = containerColor,
            disabledContainerColor = containerColor
        ),
        enabled = enabled,
        modifier = modifier
    ) {
        Text(text = answer, style = MaterialTheme.typography.titleMedium)
    }
}


@Composable
private fun NextButton(
    showNextButton: State<Boolean>,
    onClick: () -> Unit,
    onFeedbackClick: () -> Unit
) {

    val offset = animateFloatAsState(if (showNextButton.value) 0f else 1f)

    Row(
        modifier = Modifier.graphicsLayer { translationY = size.height * offset.value }
            .fillMaxWidth()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp)
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onFeedbackClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxHeight()
                .aspectRatio(1f)
        ) {
            Icon(Icons.Default.Flag, null)
        }

        Button(
            onClick = onClick,
            colors = ButtonDefaults.neutralButtonColors(),
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Text(
                text = resolveString { vocabPractice.nextButton },
                style = MaterialTheme.typography.titleMedium
            )
            Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
        }
    }

}
