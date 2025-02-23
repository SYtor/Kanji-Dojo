package ua.syt0r.kanji.presentation.screen.main.screen.info.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.PaginationLoadLaunchedEffect
import ua.syt0r.kanji.presentation.common.collectAsState
import ua.syt0r.kanji.presentation.common.trackList
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiRadicalUI
import ua.syt0r.kanji.presentation.dialog.AddWordToDeckDialog
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.info.LetterInfoData
import ua.syt0r.kanji.presentation.screen.main.screen.info.infoScreenExpandableSentenceSection
import ua.syt0r.kanji.presentation.screen.main.screen.info.infoScreenExpandableVocabSection
import ua.syt0r.kanji.presentation.screen.main.screen.info.infoScreenExpandableSection

@Composable
fun LetterInfoUI(
    letterData: LetterInfoData,
    listState: LazyListState,
    listSpacerState: ExtraListSpacerState,
    onFuriganaClick: (String) -> Unit,
    onWordClick: (JapaneseWord) -> Unit
) {

    val vocabExpanded = rememberSaveable { mutableStateOf(true) }
    val sentencesExpanded = rememberSaveable { mutableStateOf(false) }

    val vocab = letterData.vocab.collectAsState()
    val sentences = letterData.sentences.collectAsState()

    val paginateableData = listOf(
        letterData.vocab to vocabExpanded,
        letterData.sentences to sentencesExpanded
    )

    val letterHeading: LazyListScope.(LetterInfoData) -> Unit

    when (letterData) {
        is LetterInfoData.Kana -> letterHeading = {
            item {
                LetterInfoKanaHeading(
                    data = letterData,
                )
            }
        }


        is LetterInfoData.Kanji -> {
            val radicalsExpanded = rememberSaveable { mutableStateOf(true) }
            letterHeading = {
                item {
                    LetterInfoKanjiHeading(
                        data = letterData
                    )
                }
                val radicalsData = letterData.radicalsSectionData
                infoScreenExpandableSection(
                    headerText = "Radicals",
                    headerCount = radicalsData.radicals.size,
                    expanded = radicalsExpanded,
                    expandedContent = {
                        items(radicalsData.radicals) {
                            KanjiRadicalUI(
                                strokes = radicalsData.strokes,
                                radicalDetails = it,
                                onRadicalClick = onFuriganaClick
                            )
                        }

                    }
                )
            }
        }
    }

    PaginationLoadLaunchedEffect(
        listState = listState,
        prefetchDistance = InfoScreenContract.VocabListPrefetchDistance,
        loadMore = {
            val loadMoreTargetData = paginateableData
                .find { (paginateable, isExpandedState) ->
                    isExpandedState.value && paginateable.canLoadMore.value
                }
                ?.first

            loadMoreTargetData?.loadMore()
        }
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

            letterHeading(letterData)

            infoScreenExpandableVocabSection(
                expanded = vocabExpanded,
                paginateable = vocab,
                onWordClick = onWordClick,
                onFuriganaClick = onFuriganaClick,
                addWordToVocabDeckClick = { wordToAddToDeck = it }
            )

            infoScreenExpandableSentenceSection(
                expanded = sentencesExpanded,
                paginateable = sentences
            )

            listSpacerState.ExtraSpacer(this)

        }
    } else {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .trackList(listSpacerState)
        ) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                letterHeading(letterData)
                listSpacerState.ExtraSpacer(this)
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {

                infoScreenExpandableVocabSection(
                    expanded = vocabExpanded,
                    paginateable = vocab,
                    onWordClick = onWordClick,
                    onFuriganaClick = onFuriganaClick,
                    addWordToVocabDeckClick = { wordToAddToDeck = it }
                )

                infoScreenExpandableSentenceSection(
                    expanded = sentencesExpanded,
                    paginateable = sentences
                )

                listSpacerState.ExtraSpacer(this)

            }
        }

    }

}
