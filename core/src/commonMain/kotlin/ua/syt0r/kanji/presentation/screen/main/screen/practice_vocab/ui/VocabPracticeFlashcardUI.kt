package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReadMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.theme_manager.LocalThemeManager
import ua.syt0r.kanji.presentation.common.AutopaddedScrollableColumn
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.neutralTextButtonColors
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswerButtonsRow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewState

@Composable
fun VocabPracticeFlashcardUI(
    reviewState: VocabReviewState.Flashcard,
    answers: PracticeAnswers,
    onRevealAnswerClick: () -> Unit,
    onNextClick: (PracticeAnswer) -> Unit,
    onWordClick: (JapaneseWord) -> Unit
) {

    AutopaddedScrollableColumn(
        modifier = Modifier.fillMaxSize(),
        bottomOverlayContent = {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .width(IntrinsicSize.Max)
                    .height(IntrinsicSize.Max)
                    .padding(vertical = 20.dp)
            ) {

                val hiddenButton = @Composable { isVisible: Boolean ->
                    val focusRequester = remember { FocusRequester() }
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }

                    val themeModifier = when (LocalThemeManager.current.isDarkTheme) {
                        true -> Modifier.padding(4.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)

                        false -> Modifier.shadow(2.dp, MaterialTheme.shapes.medium)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface)

                    }

                    Text(
                        text = resolveString { vocabPractice.flashcardRevealButton },
                        modifier = Modifier.fillMaxSize()
                            .graphicsLayer { if (!isVisible) alpha = 0f }
                            .padding(horizontal = 20.dp)
                            .then(themeModifier)
                            .focusable()
                            .focusRequester(focusRequester)
                            .onKeyEvent {
                                if (it.type == KeyEventType.KeyUp && it.key == Key.Spacebar) {
                                    onRevealAnswerClick()
                                    true
                                } else false
                            }
                            .clickable(onClick = onRevealAnswerClick)
                            .wrapContentSize()
                    )
                }

                val revealedButton = @Composable { isVisible: Boolean ->
                    PracticeAnswerButtonsRow(
                        answers = answers,
                        onClick = { if (isVisible) onNextClick(it) },
                        modifier = Modifier.graphicsLayer { if (!isVisible) alpha = 0f },
                        contentModifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // Laying out both for static button size
                when (reviewState.showAnswer.value) {
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
    ) {

        val meaningUI = @Composable {
            Text(
                text = reviewState.meaning,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center
            )
        }

        val wordUI = @Composable { furigana: FuriganaString ->
            FuriganaText(
                furiganaString = furigana,
                textStyle = MaterialTheme.typography.displayLarge,
                annotationTextStyle = MaterialTheme.typography.bodyLarge
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (reviewState.showMeaningInFront) {

                meaningUI()

                if (reviewState.showAnswer.value) {
                    Spacer(Modifier.height(8.dp))
                    wordUI(reviewState.reading)
                }

            } else {

                val text = reviewState.run { if (showAnswer.value) reading else noFuriganaReading }
                wordUI(text)

                if (reviewState.showAnswer.value) {
                    Spacer(Modifier.height(8.dp))
                    meaningUI()
                }
            }

            val detailsAlpha = if (reviewState.showAnswer.value) 1f else 0f

            TextButton(
                onClick = { onWordClick(reviewState.word) },
                enabled = detailsAlpha != 0f,
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .graphicsLayer { alpha = detailsAlpha },
                colors = ButtonDefaults.neutralTextButtonColors()
            ) {
                Text(
                    text = resolveString { vocabPractice.detailsButton }
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ReadMore,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

        }

    }

}
