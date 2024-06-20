package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.withoutAnnotations
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AddWordToDeckDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterInputState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.MultipleStrokeInputContentState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingReviewState


data class BottomSheetStateData(
    val reviewCount: Int,
    val words: List<JapaneseWord>
)

@Composable
fun State<WritingReviewState>.asWordsBottomSheetState(): State<BottomSheetStateData> {
    return remember {
        derivedStateOf {
            val currentState = value
            val shouldRevealCharacter = when (
                val inputState = currentState.writerState.inputState) {
                is CharacterInputState.MultipleStroke -> {
                    inputState.contentState.value is MultipleStrokeInputContentState.Processed
                }

                is CharacterInputState.SingleStroke -> {
                    inputState.isStudyMode || inputState.drawnStrokesCount
                        .value == currentState.characterDetails.strokes.size
                }
            }

            val words = if (shouldRevealCharacter) {
                currentState.characterDetails.words
            } else {
                currentState.characterDetails.encodedWords
            }

            val limitedWords = words.take(WritingPracticeScreenContract.WordsLimit)

            BottomSheetStateData(
                reviewCount = currentState.practiceProgress.totalReviews,
                words = limitedWords
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingPracticeWordsBottomSheet(
    state: State<BottomSheetStateData>,
    sheetContentHeight: State<Dp>,
    onWordClick: (JapaneseWord) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(sheetContentHeight.value)
            .windowInsetsPadding(BottomSheetDefaults.windowInsets)
    ) {

        BottomSheetDefaults.DragHandle(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = resolveString { writingPractice.wordsBottomSheetTitle },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge
        )

        val currentState = state.value

        val listState = remember(currentState.reviewCount) {
            LazyListState(0)
        }

        var wordToAddToVocabDeck by remember { mutableStateOf<JapaneseWord?>(null) }
        wordToAddToVocabDeck?.let {
            AddWordToDeckDialog(
                wordId = it.id,
                wordPreviewReading = it.readings.first().withoutAnnotations(),
                onDismissRequest = { wordToAddToVocabDeck = null }
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState
        ) {

            itemsIndexed(currentState.words) { index, word ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .padding(horizontal = 12.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onWordClick(word) }
                        .padding(vertical = 4.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FuriganaText(
                        furiganaString = word.orderedPreview(index),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { wordToAddToVocabDeck = word }) {
                        Icon(Icons.Default.AddCircleOutline, null)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

        }

    }

}