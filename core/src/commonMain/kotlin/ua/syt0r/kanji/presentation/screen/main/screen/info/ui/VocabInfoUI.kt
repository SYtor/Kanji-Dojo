package ua.syt0r.kanji.presentation.screen.main.screen.info.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.http.buildUrl
import org.jetbrains.compose.resources.painterResource
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.baseline_open_in_new_24
import ua.syt0r.kanji.core.app_data.data.DetailedVocabSense
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.VocabReading
import ua.syt0r.kanji.core.app_data.data.formattedKanaReading
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.PaginationLoadLaunchedEffect
import ua.syt0r.kanji.presentation.common.collectAsState
import ua.syt0r.kanji.presentation.common.rememberUrlHandler
import ua.syt0r.kanji.presentation.common.theme.neutralTextButtonColors
import ua.syt0r.kanji.presentation.common.trackList
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.common.ui.kanji.ClickableLetter
import ua.syt0r.kanji.presentation.dialog.AddWordToDeckDialog
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenPaddedListIndex
import ua.syt0r.kanji.presentation.screen.main.screen.info.VocabInfoData
import ua.syt0r.kanji.presentation.screen.main.screen.info.infoScreenExpandableSection
import ua.syt0r.kanji.presentation.screen.main.screen.info.infoScreenExpandableSentenceSection

@Composable
fun VocabInfoUI(
    vocabData: VocabInfoData,
    listState: LazyListState,
    listSpacerState: ExtraListSpacerState,
    onLetterClick: (String) -> Unit
) {

    val senseExpanded = rememberSaveable { mutableStateOf(true) }
    val lettersExpanded = rememberSaveable { mutableStateOf(false) }
    val sentencesExpanded = rememberSaveable { mutableStateOf(true) }
    val sentences = vocabData.sentences.collectAsState()

    PaginationLoadLaunchedEffect(
        listState = listState,
        loadMore = { vocabData.sentences.loadMore() }
    )

    when (LocalOrientation.current) {
        Orientation.Portrait -> {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .trackList(listSpacerState)
            ) {

                item { VocabReadingSection(vocabData.word) }

                expandableSenseSection(
                    matchingSenses = vocabData.matchingSenses,
                    expanded = senseExpanded
                )

                expandableVocabLettersSection(
                    reading = vocabData.word.reading,
                    onLetterClick = onLetterClick,
                    expanded = lettersExpanded
                )

                infoScreenExpandableSentenceSection(
                    expanded = sentencesExpanded,
                    paginateable = sentences
                )

                listSpacerState.ExtraSpacer(this)

            }
        }

        Orientation.Landscape -> {

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
                    item { VocabReadingSection(vocabData.word) }

                    expandableSenseSection(
                        matchingSenses = vocabData.matchingSenses,
                        expanded = senseExpanded
                    )

                    expandableVocabLettersSection(
                        reading = vocabData.word.reading,
                        onLetterClick = onLetterClick,
                        expanded = lettersExpanded
                    )

                    listSpacerState.ExtraSpacer(this)
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {

                    infoScreenExpandableSentenceSection(
                        expanded = sentencesExpanded,
                        paginateable = sentences
                    )

                    listSpacerState.ExtraSpacer(this)

                }

            }
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VocabReadingSection(word: JapaneseWord) {

    var showAddToDeckDialog by rememberSaveable { mutableStateOf(false) }
    if (showAddToDeckDialog) {
        AddWordToDeckDialog(
            word = word,
            onDismissRequest = { showAddToDeckDialog = false }
        )
    }

    val urlHandler = rememberUrlHandler()

    val readingStyle = MaterialTheme.typography.headlineLarge.copy(textAlign = TextAlign.Center)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val reading = word.reading

        when {
            reading.furigana != null -> {
                FuriganaText(
                    furiganaString = reading.furigana,
                    textStyle = readingStyle
                )
            }

            reading.kanjiReading != null -> {
                Text(
                    text = reading.kanjiReading,
                    style = readingStyle
                )
                Text(
                    text = formattedKanaReading(reading.kanaReading),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                Text(
                    text = reading.kanaReading,
                    style = readingStyle
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = { showAddToDeckDialog = true },
                colors = ButtonDefaults.neutralTextButtonColors()
            ) {
                Text("Add to deck", Modifier.padding(end = 8.dp))
                Icon(Icons.Default.Add, null)
            }

            TextButton(
                onClick = {
                    val searchTerm = reading.kanjiReading ?: reading.kanaReading
                    val url = buildUrl {
                        protocol = URLProtocol.HTTPS
                        host = "jisho.org"
                        appendPathSegments("search", searchTerm)
                    }
                    urlHandler.openInBrowser(url.toString())
                },
                colors = ButtonDefaults.neutralTextButtonColors()
            ) {
                Text("Jisho", Modifier.padding(end = 8.dp))
                Icon(painterResource(Res.drawable.baseline_open_in_new_24), null)
            }
        }

    }
}


@OptIn(ExperimentalLayoutApi::class)
private fun LazyListScope.expandableVocabLettersSection(
    reading: VocabReading,
    onLetterClick: (String) -> Unit,
    expanded: MutableState<Boolean>
) {

    val text = reading.kanjiReading ?: reading.kanaReading
    val letters = text.map { it.toString() }.distinct()

    infoScreenExpandableSection(
        headerText = "Letters",
        headerCount = letters.size,
        expanded = expanded,
        expandedContent = {
            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    letters.forEach {
                        ClickableLetter(
                            letter = it,
                            onClick = onLetterClick
                        )
                    }
                }
            }
        }
    )
}

private fun LazyListScope.expandableSenseSection(
    matchingSenses: List<DetailedVocabSense>,
    expanded: MutableState<Boolean>
) {
    infoScreenExpandableSection(
        headerText = "Sense",
        headerCount = matchingSenses.size,
        expanded = expanded,
        expandedContent = {
            itemsIndexed(matchingSenses) { i, sense ->
                ListItem(
                    leadingContent = { InfoScreenPaddedListIndex(i) },
                    headlineContent = { Text(sense.glossary.joinToString()) },
                    supportingContent = { Text(sense.partOfSpeechList.joinToString()) }
                )
            }
        }
    )
}


