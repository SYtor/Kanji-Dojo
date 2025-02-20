package ua.syt0r.kanji.presentation.screen.main.screen.info.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.PaginationLoadLaunchedEffect
import ua.syt0r.kanji.presentation.common.collectAsState
import ua.syt0r.kanji.presentation.common.trackList
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.dialog.AddWordToDeckDialog
import ua.syt0r.kanji.presentation.screen.main.screen.info.LetterInfoData
import ua.syt0r.kanji.presentation.screen.main.screen.info.expandableInfoVocabSection

@Composable
fun LetterInfoUI(
    letterData: LetterInfoData,
    listState: LazyListState,
    listSpacerState: ExtraListSpacerState,
    onCopyButtonClick: () -> Unit,
    onFuriganaClick: (String) -> Unit,
    onWordClick: (JapaneseWord) -> Unit
) {

    val vocabExpanded = rememberSaveable { mutableStateOf(true) }
    val vocab = letterData.vocab.collectAsState()

    PaginationLoadLaunchedEffect(
        listState = listState,
        loadMore = { letterData.vocab.loadMore() }
    )

    var wordToAddToDeck by remember { mutableStateOf<JapaneseWord?>(null) }
    wordToAddToDeck?.let {
        AddWordToDeckDialog(
            word = it,
            onDismissRequest = { wordToAddToDeck = null }
        )
    }

    if (LocalOrientation.current == Orientation.Portrait) {

        LazyColumn(
            state = listState,
            modifier = Modifier.trackList(listSpacerState)
        ) {

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    LetterInfoHeadingUI(
                        letterData = letterData,
                        onCopyButtonClick = onCopyButtonClick,
                        onRadicalClick = onFuriganaClick
                    )
                }
            }

            expandableInfoVocabSection(
                expanded = vocabExpanded,
                paginateable = vocab,
                onWordClick = onWordClick,
                onFuriganaClick = onFuriganaClick,
                addWordToVocabDeckClick = { wordToAddToDeck = it }
            )

            listSpacerState.ExtraSpacer(this)

        }
    } else {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .trackList(listSpacerState)
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {

                LetterInfoHeadingUI(
                    letterData = letterData,
                    onCopyButtonClick = onCopyButtonClick,
                    onRadicalClick = onFuriganaClick
                )

                listSpacerState.ExtraSpacer()

            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {

                expandableInfoVocabSection(
                    expanded = vocabExpanded,
                    paginateable = vocab,
                    onWordClick = onWordClick,
                    onFuriganaClick = onFuriganaClick,
                    addWordToVocabDeckClick = { wordToAddToDeck = it }
                )

                listSpacerState.ExtraSpacer(this)

            }
        }

    }

}