package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.JapaneseWordUI
import ua.syt0r.kanji.presentation.dialog.AddWordToDeckDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWritingProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.LetterPracticeScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeExampleWord
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState


data class BottomSheetStateData(
    val letter: String,
    val reveal: Boolean,
    val words: List<LetterPracticeExampleWord>
)

@Composable
fun State<LetterPracticeReviewState.Writing>.asWordsBottomSheetState(): State<BottomSheetStateData> {
    return remember {
        derivedStateOf {
            val currentState = value
            val writerState = currentState.writerState.value
            val isStudyMode = writerState.configuration
                .run { this is CharacterWriterConfiguration.StrokeInput && isStudyMode }
            val revealCharacter = writerState.progress
                .value !is CharacterWritingProgress.Writing

            val limitedWords = currentState.itemData.words
                .take(LetterPracticeScreenContract.WordsLimit)

            BottomSheetStateData(
                letter = writerState.character,
                reveal = isStudyMode || revealCharacter,
                words = limitedWords
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterPracticeWritingWordsBottomSheet(
    state: State<BottomSheetStateData>,
    sheetContentHeight: State<Dp>,
    onWordClick: (JapaneseWord) -> Unit
) {

    var wordToAddToVocabDeck by remember { mutableStateOf<JapaneseWord?>(null) }
    wordToAddToVocabDeck?.let {
        AddWordToDeckDialog(
            word = it,
            onDismissRequest = { wordToAddToVocabDeck = null }
        )
    }

    val windowBottomExtraPaddingDp = WindowInsets.safeContent
        .asPaddingValues()
        .calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = sheetContentHeight.value - windowBottomExtraPaddingDp)
            .windowInsetsPadding(BottomSheetDefaults.windowInsets)
    ) {

        BottomSheetDefaults.DragHandle(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        val currentState by state
        val listState = remember(currentState) { LazyListState(0) }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState
        ) {

            itemsIndexed(currentState.words) { index, word ->
                JapaneseWordUI(
                    index = index,
                    word = word.word,
                    headline = {
                        WritingPracticeVocabHeadline(
                            word = word,
                            reveal = currentState.reveal,
                            letter = currentState.letter
                        )
                    },
                    onClick = { onWordClick(word.word) }
                        .takeIf { currentState.reveal },
                    addWordToVocabDeckClick = { wordToAddToVocabDeck = word.word }
                        .takeIf { currentState.reveal }
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

        }

    }

}