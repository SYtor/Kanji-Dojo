package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.CenteredBoxWithSide
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.ExpandablePracticeAnswerButtonsRow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.ExpandableVocabPracticeAnswersRowState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.SelectedReadingAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReference

@Composable
fun VocabPracticeReadingPickerUI(
    reviewState: VocabReviewState.Reading,
    answers: PracticeAnswers,
    onWordClick: (VocabReference) -> Unit,
    onAnswerSelected: (String) -> Unit,
    onNextClick: (PracticeAnswer) -> Unit,
    onFeedbackClick: (JapaneseWord) -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {

        val selectedAnswer by reviewState.selectedAnswer

        FuriganaText(
            furiganaString = reviewState.displayReading.value,
            textStyle = MaterialTheme.typography.displayLarge,
            annotationTextStyle = MaterialTheme.typography.bodyLarge,
        )

        if (selectedAnswer != null || reviewState.showMeaning) {
            CenteredBoxWithSide(
                modifier = Modifier.widthIn(max = 400.dp),
                placeSideContentAtStart = false,
                centerContent = {
                    Text(
                        text = reviewState.meaning,
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                sideContent = {
                    reviewState.vocabReference?.let {
                        IconButton(
                            enabled = selectedAnswer != null,
                            onClick = { onWordClick(it) }
                        ) {
                            Icon(Icons.Default.ArrowOutward, null)
                        }
                    }

                }
            )
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

                    val missingSpots = maxItemsInEachRow - rowAnswers.size
                    if (missingSpots != 0)
                        Spacer(Modifier.weight(missingSpots.toFloat()))

                }

            }
        }

        val updatedState = rememberUpdatedState(reviewState to answers)

        ExpandablePracticeAnswerButtonsRow(
            state = remember {
                derivedStateOf {
                    val (state, updatedAnswers) = updatedState.value
                    ExpandableVocabPracticeAnswersRowState(
                        answers = updatedAnswers,
                        showButton = state.selectedAnswer.value != null
                    )
                }
            },
            onClick = {
                val updatedAnswer = it.copy(
                    mistakes = if (selectedAnswer!!.isCorrect) 0 else 1
                )
                onNextClick(updatedAnswer)
            },
            modifier = Modifier.width(IntrinsicSize.Max)
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
