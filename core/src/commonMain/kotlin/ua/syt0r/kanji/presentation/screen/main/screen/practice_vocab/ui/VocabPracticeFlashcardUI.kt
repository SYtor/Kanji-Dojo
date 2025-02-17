package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.presentation.common.AutopaddedScrollableColumn
import ua.syt0r.kanji.presentation.common.ui.CenteredBoxWithSide
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.FlashcardPracticeAnswerButtonsRow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReference

@Composable
fun VocabPracticeFlashcardUI(
    reviewState: VocabReviewState.Flashcard,
    answers: PracticeAnswers,
    onRevealAnswerClick: () -> Unit,
    onNextClick: (PracticeAnswer) -> Unit,
    onWordClick: (VocabReference) -> Unit
) {

    AutopaddedScrollableColumn(
        modifier = Modifier.fillMaxSize(),
        bottomOverlayContent = {
            FlashcardPracticeAnswerButtonsRow(
                answers = answers,
                showAnswer = reviewState.showAnswer,
                onRevealAnswerClick = onRevealAnswerClick,
                onAnswerClick = onNextClick
            )
        }
    ) {

        val meaningUI = @Composable {
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
                            enabled = reviewState.showAnswer.value,
                            onClick = { onWordClick(it) }
                        ) {
                            Icon(Icons.Default.ArrowOutward, null)
                        }
                    }

                }
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

        }

    }

}
