package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.withoutAnnotations
import ua.syt0r.kanji.presentation.common.CollapsibleContainer
import ua.syt0r.kanji.presentation.common.CollapsibleContainerState
import ua.syt0r.kanji.presentation.common.isNearListEnd
import ua.syt0r.kanji.presentation.common.jsonSaver
import ua.syt0r.kanji.presentation.common.rememberCollapsibleContainerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.trackItemPosition
import ua.syt0r.kanji.presentation.common.ui.ClickableFuriganaText
import ua.syt0r.kanji.presentation.dialog.AddWordToDeckDialog
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.RadicalSearchState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchScreenUI(
    state: State<ScreenState>,
    radicalsState: State<RadicalSearchState>,
    onSubmitInput: (String) -> Unit,
    onRadicalsSectionExpanded: () -> Unit,
    onRadicalsSelected: (Set<String>) -> Unit,
    onCharacterClick: (String) -> Unit,
    onScrolledToEnd: () -> Unit,
    onWordFeedback: (JapaneseWord) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val inputState = rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    val selectedRadicalsState = rememberSaveable() {
        mutableStateOf(emptySet<String>())
    }

    LaunchedEffect(Unit) {
        snapshotFlow { inputState.value }
            .onEach { onSubmitInput(it.text) }
            .launchIn(this)
        snapshotFlow { selectedRadicalsState.value }
            .onEach { onRadicalsSelected(it) }
            .launchIn(this)
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            Surface {
                RadicalSearch(
                    state = radicalsState,
                    selectedRadicals = selectedRadicalsState,
                    onCharacterClick = {
                        inputState.value = inputState.value.run {
                            TextFieldValue(
                                text = text + it,
                                selection = TextRange(text.length + 1)
                            )
                        }
                    }
                )
            }
        }
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            val searchContainerState = rememberCollapsibleContainerState()

            CollapsibleContainer(searchContainerState) {
                InputSection(
                    inputState = inputState,
                    onOpenRadicalSearch = {
                        coroutineScope.launch {
                            sheetState.show()
                            onRadicalsSectionExpanded()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            ) {
                val isProgressVisible = remember { derivedStateOf { state.value.isLoading } }
                androidx.compose.animation.AnimatedVisibility(
                    visible = isProgressVisible.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        trackColor = MaterialTheme.colorScheme.background
                    )
                }
            }

            var selectedWord by rememberSaveable(stateSaver = jsonSaver()) {
                mutableStateOf<JapaneseWord?>(null)
            }

            selectedWord?.also {
                AlternativeWordsDialog(
                    word = it,
                    onDismissRequest = { selectedWord = null },
                    onFuriganaClick = onCharacterClick,
                    onFeedbackClick = { onWordFeedback(it) }
                )
            }

            ListContent(
                screenState = state.value,
                searchContainerState = searchContainerState,
                onCharacterClick = onCharacterClick,
                onWordClick = { selectedWord = it },
                onScrolledToEnd = onScrolledToEnd
            )

        }

    }

}

@Composable
private fun InputSection(
    inputState: MutableState<TextFieldValue>,
    onOpenRadicalSearch: () -> Unit,
    modifier: Modifier
) {

    var enteredText by inputState
    val interactionSource = remember { MutableInteractionSource() }
    val isInputFocused = remember { mutableStateOf(false) }

    val isHintVisible = remember {
        derivedStateOf { !isInputFocused.value && enteredText.text.isEmpty() }
    }
    val color = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onOpenRadicalSearch
        ) {
            Text(text = "部")
        }
        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = enteredText,
                onValueChange = { enteredText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isInputFocused.value = it.isFocused },
                maxLines = 1,
                singleLine = true,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(color),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color)
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = isHintVisible.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = resolveString { search.inputHint },
                    style = MaterialTheme.typography.titleMedium,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
        IconButton(
            onClick = { enteredText = TextFieldValue() }
        ) {
            Icon(Icons.Default.Close, null)
        }

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListContent(
    screenState: ScreenState,
    searchContainerState: CollapsibleContainerState,
    onCharacterClick: (String) -> Unit,
    onWordClick: (JapaneseWord) -> Unit,
    onScrolledToEnd: () -> Unit
) {

    val listState = rememberLazyListState()

    val canLoadMoreWords = remember(screenState) {
        derivedStateOf { screenState.words.value.canLoadMore }
    }

    if (canLoadMoreWords.value) {
        LaunchedEffect(Unit) {
            snapshotFlow {
                listState.isNearListEnd(SearchScreenContract.LoadMoreWordsFromEndThreshold)
            }
                .filter { it }
                .collect { onScrolledToEnd() }
        }
    }

    val shouldShowScrollUpButton = remember {
        derivedStateOf { listState.firstVisibleItemIndex > 10 }
    }

    var wordToAddToVocabDeck by remember { mutableStateOf<JapaneseWord?>(null) }
    wordToAddToVocabDeck?.let {
        AddWordToDeckDialog(
            wordId = it.id,
            wordPreviewReading = it.readings.first().withoutAnnotations(),
            onDismissRequest = { wordToAddToVocabDeck = null }
        )
    }

    Box {

        val contentBottomPadding = remember { mutableStateOf(0.dp) }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
                .nestedScroll(searchContainerState.nestedScrollConnection)
        ) {

            item {
                Text(
                    text = resolveString { search.charactersTitle(screenState.characters.size) },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .wrapContentSize(Alignment.CenterStart)
                )
            }

            item {

                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    item { Spacer(modifier = Modifier.width(20.dp)) }
                    items(screenState.characters) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier
                                .padding(end = 20.dp)
                                .padding(vertical = 16.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onCharacterClick(it) }
                                .padding(8.dp)
                                .height(IntrinsicSize.Min)
                                .aspectRatio(1f, true)
                                .wrapContentSize()
                        )
                    }
                }

            }

            val currentWordsState = screenState.words.value

            stickyHeader {
                Text(
                    text = resolveString { search.wordsTitle(currentWordsState.totalCount) },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .wrapContentSize(Alignment.CenterStart)
                )
            }

            itemsIndexed(currentWordsState.items) { index, word ->
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
                    ClickableFuriganaText(
                        furiganaString = word.orderedPreview(index),
                        onClick = { onCharacterClick(it) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { wordToAddToVocabDeck = word }) {
                        Icon(Icons.Default.AddCircleOutline, null)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(contentBottomPadding.value + 16.dp)) }

        }

        AnimatedVisibility(
            visible = shouldShowScrollUpButton.value,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .trackItemPosition { contentBottomPadding.value = it.heightFromScreenBottom }
        ) {
            val coroutineScope = rememberCoroutineScope()
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch { listState.scrollToItem(0) }
                    coroutineScope.launch { searchContainerState.expand() }
                }
            ) {
                Icon(Icons.Default.KeyboardArrowUp, null)
            }
        }

    }

}
