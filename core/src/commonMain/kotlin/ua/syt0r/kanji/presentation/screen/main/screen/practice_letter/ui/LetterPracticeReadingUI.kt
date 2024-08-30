package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.withoutAnnotations
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.dialog.AddWordToDeckDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.FlashcardPracticeAnswerButtonsRow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState

@Composable
fun LetterPracticeReadingUI(
    reviewState: LetterPracticeReviewState.Reading,
    onNextClick: (PracticeAnswer) -> Unit,
    speakKana: (KanaReading) -> Unit,
    onWordClick: (JapaneseWord) -> Unit
) {

    var wordToAddToVocabDeck by remember { mutableStateOf<JapaneseWord?>(null) }
    wordToAddToVocabDeck?.let {
        AddWordToDeckDialog(
            wordId = it.id,
            wordPreviewReading = it.readings.first().withoutAnnotations(),
            onDismissRequest = { wordToAddToVocabDeck = null }
        )
    }

    val words = when (reviewState.revealed.value) {
        true -> reviewState.itemData.words
        false -> reviewState.itemData.encodedWords
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (LocalOrientation.current == Orientation.Portrait) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                item {
                    ReadingPracticeCharacterDetailsUI(
                        state = reviewState,
                        speakKana = speakKana,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    )
                }
                addWordItems(
                    words = words,
                    onWordClick = onWordClick,
                    addWordToDeck = { wordToAddToVocabDeck = it }
                )
            }
        } else {

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {

                ReadingPracticeCharacterDetailsUI(
                    state = reviewState,
                    speakKana = speakKana,
                    modifier = Modifier.weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                )

                VerticalDivider(color = MaterialTheme.colorScheme.outline)

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    addWordItems(
                        words = words,
                        onWordClick = onWordClick,
                        addWordToDeck = { wordToAddToVocabDeck = it }
                    )
                }

            }

        }

        FlashcardPracticeAnswerButtonsRow(
            answers = reviewState.answers,
            showAnswer = reviewState.revealed,
            onRevealAnswerClick = { reviewState.revealed.value = true },
            onAnswerClick = onNextClick,
            modifier = Modifier.padding(vertical = 20.dp).align(Alignment.CenterHorizontally)
        )

    }


}


@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.addWordItems(
    words: List<JapaneseWord>,
    onWordClick: (JapaneseWord) -> Unit,
    addWordToDeck: (JapaneseWord) -> Unit
) {

    stickyHeader {

        Text(
            text = resolveString { letterPractice.headerWordsMessage(words.size) },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        )

    }

    itemsIndexed(words) { index, word ->
        LetterPracticeWordRow(
            index = index,
            word = word,
            onWordClick = onWordClick,
            addWordToDeckClick = addWordToDeck
        )
    }

    item { Spacer(modifier = Modifier.height(20.dp)) }

}
