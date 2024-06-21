package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReadMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.AutopaddedScrollableColumn
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.neutralButtonColors
import ua.syt0r.kanji.presentation.common.theme.neutralTextButtonColors
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewState

@Composable
fun VocabPracticeFlashcardUI(
    reviewState: VocabReviewState.Flashcard,
    onRevealAnswerClick: () -> Unit,
    onNextClick: () -> Unit,
    onWordClick: (JapaneseWord) -> Unit
) {

    AutopaddedScrollableColumn(
        modifier = Modifier.fillMaxSize()
            .wrapContentWidth()
            .widthIn(max = 400.dp),
        bottomOverlayContent = {
            Button(
                onClick = {
                    when (reviewState.showAnswer.value) {
                        true -> onNextClick()
                        false -> onRevealAnswerClick()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                colors = ButtonDefaults.neutralButtonColors()
            ) {
                Text(
                    text = when (reviewState.showAnswer.value) {
                        true -> "Next"
                        false -> "Show answer"
                    }
                )
            }
        }
    ) {

        val meaningUI = @Composable {
            Text(
                text = reviewState.meaning,
                style = MaterialTheme.typography.displaySmall
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

            val detailsAlpha = if (reviewState.showAnswer.value) 1f else 0f

            TextButton(
                onClick = { onWordClick(reviewState.word) },
                enabled = detailsAlpha != 0f,
                modifier = Modifier.align(Alignment.End).graphicsLayer { alpha = detailsAlpha },
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

        }

    }

}