package ua.syt0r.kanji.presentation.screen.main.screen.info

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.app_data.Sentence
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.ExpandButton
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.JapaneseWordUI
import ua.syt0r.kanji.presentation.common.PaginateableState
import ua.syt0r.kanji.presentation.common.clickable
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.trackOverlay
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.info.ui.LetterInfoUI


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreenUI(
    state: State<ScreenState>,
    onUpButtonClick: () -> Unit,
    onFuriganaClick: (String) -> Unit,
    onWordClick: (JapaneseWord) -> Unit
) {

    ScreenLayout(
        state = state,
        toolbar = {
            TopAppBar(
                title = { Text(text = "") },
                navigationIcon = {
                    IconButton(onClick = onUpButtonClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        letter = { data, listState, listSpacerState ->

            LetterInfoUI(
                letterData = data,
                listState = listState,
                listSpacerState = listSpacerState,
                onFuriganaClick = onFuriganaClick,
                onWordClick = onWordClick
            )

        }
    )

}


@Composable
private fun ScreenLayout(
    state: State<ScreenState>,
    toolbar: @Composable () -> Unit,
    letter: @Composable (LetterInfoData, LazyListState, ExtraListSpacerState) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val listState = rememberLazyListState()

    val extraListSpacerState = rememberExtraListSpacerState()

    val shouldShowScrollButton = remember {
        derivedStateOf { listState.firstVisibleItemIndex != 0 }
    }

    Scaffold(
        topBar = { toolbar() },
        floatingActionButton = {
            AnimatedVisibility(
                visible = shouldShowScrollButton.value,
                enter = scaleIn(),
                exit = scaleOut(),
                modifier = Modifier.trackOverlay(extraListSpacerState)
            ) {
                FloatingActionButton(
                    onClick = { coroutineScope.launch { listState.scrollToItem(0) } }
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {

        AnimatedContent(
            targetState = state.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) { screenState ->

            when (screenState) {

                ScreenState.Loading -> {
                    LoadingState()
                }

                ScreenState.NoData -> {
                    Text(
                        text = resolveString { info.noDataMessage },
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                    )
                }

                is ScreenState.Loaded.Letter -> {
                    letter(screenState.data, listState, extraListSpacerState)
                }

            }

        }

    }


}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun <T> LazyListScope.expandableSection(
    expanded: MutableState<Boolean>,
    paginateable: PaginateableState<T>,
    header: @Composable () -> Unit,
    item: @Composable (Int, T) -> Unit
) {


    stickyHeader {
        ExpandableSectionHeader(expanded, header)
    }

    if (expanded.value) {

        itemsIndexed(paginateable.list) { index, listItem -> item(index, listItem) }

        if (paginateable.canLoadMore) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .wrapContentSize()
                )
            }
        }

    }

}

@Composable
fun ExpandableSectionHeader(
    expanded: MutableState<Boolean>,
    header: @Composable () -> Unit
) {
    val toggleExpanded = { expanded.value = expanded.value.not() }

    ListItem(
        headlineContent = {
            header()
        },
        leadingContent = {
            ExpandButton(
                expanded = expanded.value,
                onClick = toggleExpanded
            )
        },
        modifier = Modifier
            .clickable(toggleExpanded)
    )
}

fun LazyListScope.expandableInfoVocabSection(
    expanded: MutableState<Boolean>,
    paginateable: PaginateableState<JapaneseWord>,
    onWordClick: (JapaneseWord) -> Unit,
    addWordToVocabDeckClick: (JapaneseWord) -> Unit,
    onFuriganaClick: (String) -> Unit
) {

    expandableSection(
        expanded = expanded,
        paginateable = paginateable,
        header = {
            Text(
                text = resolveString { info.wordsSectionTitle(paginateable.total) },
            )
        },
        item = { index, word ->
            JapaneseWordUI(
                index = index,
                word = word,
                onClick = { onWordClick(word) },
                onFuriganaClick = onFuriganaClick,
                addWordToVocabDeckClick = { addWordToVocabDeckClick(word) }
            )
        }
    )

}

fun LazyListScope.expandableInfoSentenceSection(
    expanded: MutableState<Boolean>,
    paginateable: PaginateableState<Sentence>
) {

    expandableSection(
        expanded = expanded,
        paginateable = paginateable,
        header = { Text("Sentences (${paginateable.total})") },
        item = { index, sentence ->
            ListItem(
                leadingContent = {
                    Text(
                        text = (index + 1).toString(),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                headlineContent = { Text(sentence.value) },
                supportingContent = { Text(sentence.translation) }
            )
        }
    )

}

@Composable
fun InfoScreenExpandableSection(
    expanded: MutableState<Boolean>,
    header: @Composable () -> Unit,
    expandedContent: @Composable() (ColumnScope.() -> Unit)
) {

    Column {

        ExpandableSectionHeader(
            expanded = expanded,
            header = header
        )

        if (!expanded.value) return@Column

        expandedContent()

    }

}
