package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.data.MultipleStrokeInputState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.data.WritingReviewState


data class BottomSheetStateData(
    val reviewCount: Int,
    val words: List<JapaneseWord>
)

@Composable
fun State<WritingReviewState>.asWordsBottomSheetState(): State<BottomSheetStateData> {
    return remember {
        derivedStateOf {
            val currentState = value
            val shouldRevealCharacter = when (currentState) {
                is WritingReviewState.MultipleStrokeInput -> {
                    currentState.inputState.value is MultipleStrokeInputState.Processed
                }

                is WritingReviewState.SingleStrokeInput -> {
                    currentState.isStudyMode || currentState.drawnStrokesCount
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
    ) {

        Box(
            modifier = Modifier
                .padding(top = 10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline)
                .size(width = 60.dp, height = 4.dp)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = resolveString { writingPractice.wordsBottomSheetTitle },
            modifier = Modifier
                .padding(top = 8.dp, bottom = 16.dp)
                .padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleLarge
        )


        val currentState = state.value

        val listState = remember(currentState.reviewCount) {
            LazyListState(0)
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState
        ) {

            itemsIndexed(currentState.words) { index, word ->
                FuriganaText(
                    furiganaString = word.orderedPreview(index),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .heightIn(min = 50.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(onClick = { onWordClick(word) })
                        .padding(horizontal = 10.dp)
                        .wrapContentSize(Alignment.CenterStart)
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

        }

    }

}