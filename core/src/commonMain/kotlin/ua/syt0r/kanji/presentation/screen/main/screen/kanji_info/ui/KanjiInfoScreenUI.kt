package ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ua.syt0r.kanji.common.db.entity.CharacterRadical
import ua.syt0r.kanji.core.kanji_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.ItemHeightData
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.trackScreenHeight
import ua.syt0r.kanji.presentation.common.ui.AutoBreakRow
import ua.syt0r.kanji.presentation.common.ui.ClickableFuriganaText
import ua.syt0r.kanji.presentation.common.ui.kanji.RadicalKanji
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.KanjiInfoScreenContract.ScreenState


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun KanjiInfoScreenUI(
    char: String,
    state: State<ScreenState>,
    onUpButtonClick: () -> Unit,
    onCopyButtonClick: () -> Unit,
    onFuriganaItemClick: (String) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val listState = rememberLazyListState()
    val fabHeightData = remember { mutableStateOf<ItemHeightData?>(null) }

    val shouldShowScrollButton = remember {
        derivedStateOf(policy = structuralEqualityPolicy()) {
            listState.firstVisibleItemIndex != 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SelectionContainer { Text(text = char) }
                },
                navigationIcon = {
                    IconButton(onClick = onUpButtonClick) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = shouldShowScrollButton.value,
                enter = scaleIn(),
                exit = scaleOut(),
                modifier = Modifier.trackScreenHeight { fabHeightData.value = it }
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

        Crossfade(
            targetState = state.value,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
        ) { screenState ->

            when (screenState) {

                ScreenState.Loading -> {
                    LoadingState()
                }

                ScreenState.NoData -> {
                    Text(
                        text = resolveString { kanjiInfo.noDataMessage },
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                    )
                }

                is ScreenState.Loaded -> {
                    val clipboardCopyMessage = resolveString { kanjiInfo.clipboardCopyMessage }
                    LoadedState(
                        screenState = screenState,
                        listState = listState,
                        fabHeightData = fabHeightData,
                        onCopyButtonClick = {
                            onCopyButtonClick()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    clipboardCopyMessage,
                                    withDismissAction = true
                                )
                            }
                        },
                        onFuriganaItemClick = onFuriganaItemClick
                    )
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

@Composable
private fun LoadedState(
    screenState: ScreenState.Loaded,
    listState: LazyListState,
    fabHeightData: State<ItemHeightData?>,
    onCopyButtonClick: () -> Unit,
    onFuriganaItemClick: (String) -> Unit,
    defaultRadicalsExpanded: Boolean = true,
    defaultWordsExpanded: Boolean = false
) {

    var radicalsExpanded by rememberSaveable { mutableStateOf(defaultRadicalsExpanded) }
    var wordsExpanded by rememberSaveable { mutableStateOf(defaultWordsExpanded) }

    val selectedWordForAlternativeDialog = rememberSaveable {
        mutableStateOf<JapaneseWord?>(null)
    }
    selectedWordForAlternativeDialog.value?.let {
        AlternativeWordsDialog(
            word = it,
            onDismissRequest = { selectedWordForAlternativeDialog.value = null },
            onFuriganaClick = onFuriganaItemClick
        )
    }

    val indexedWords = remember {
        screenState.words.mapIndexed { index, word -> index to word }
    }

    LazyColumn(
        state = listState
    ) {

        item {
            KanjiInfoCharacterInfoSection(
                screenState = screenState,
                onCopyButtonClick = onCopyButtonClick
            )
        }

        item {
            ExpandableSectionHeader(
                text = resolveString { kanjiInfo.radicalsSectionTitle(screenState.radicals.size) },
                isExpanded = radicalsExpanded,
                toggleExpandedState = { radicalsExpanded = !radicalsExpanded }
            )
        }

        if (radicalsExpanded) {
            item {
                RadicalsSectionContent(
                    strokes = screenState.strokes,
                    radicals = screenState.radicals
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            ExpandableSectionHeader(
                text = resolveString { kanjiInfo.wordsSectionTitle(screenState.words.size) },
                isExpanded = wordsExpanded,
                toggleExpandedState = { wordsExpanded = !wordsExpanded }
            )
        }

        if (wordsExpanded) {

            item { Spacer(modifier = Modifier.height(16.dp)) }

            items(indexedWords) { (index, word) ->
                ExpressionItem(
                    index = index,
                    word = word,
                    onFuriganaItemClick = onFuriganaItemClick,
                    onAlternativeButtonClick = { selectedWordForAlternativeDialog.value = word }
                )
            }

            item { ExtraListBottomSpacer(fabHeightData = fabHeightData) }

        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

    }

}

@Composable
private fun ExpandableSectionHeader(
    text: String,
    isExpanded: Boolean,
    toggleExpandedState: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = toggleExpandedState)
            .padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        IconButton(onClick = toggleExpandedState) {
            val rotation by animateFloatAsState(if (isExpanded) 0f else 180f)
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                modifier = Modifier.graphicsLayer(rotationZ = rotation)
            )
        }
    }

}

@Composable
private fun RadicalsSectionContent(
    strokes: List<Path>,
    radicals: List<CharacterRadical>
) {

    Row(
        Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
    ) {

        Box(
            modifier = Modifier.size(120.dp)
        ) {

            RadicalKanji(
                strokes = strokes,
                radicals = radicals,
                modifier = Modifier.fillMaxSize()
            )

        }

        if (radicals.isEmpty()) {

            Text(
                text = resolveString { kanjiInfo.noRadicalsMessage },
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .height(120.dp)
                    .weight(1f)
                    .wrapContentSize()
            )

        } else {
            AutoBreakRow(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {

                radicals.forEach {
                    Text(
                        text = it.radical,
                        fontSize = 32.sp,
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable {}
                            .padding(8.dp)
                            .width(IntrinsicSize.Min)
                            .aspectRatio(1f, true)
                            .wrapContentSize(unbounded = true)
                    )
                }
            }
        }

    }

}


@Composable
private fun ExpressionItem(
    index: Int,
    word: JapaneseWord,
    onFuriganaItemClick: (String) -> Unit,
    onAlternativeButtonClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onAlternativeButtonClick)
    ) {
        SelectionContainer(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            ClickableFuriganaText(
                furiganaString = word.orderedPreview(index),
                onClick = onFuriganaItemClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )
        }
    }

}

@Composable
private fun ExtraListBottomSpacer(
    fabHeightData: State<ItemHeightData?>
) {

    val bottomPadding = remember {
        derivedStateOf { fabHeightData.value?.heightFromScreenBottom ?: 16.dp }
    }

    Spacer(modifier = Modifier.height(bottomPadding.value))

}

//@Preview
//@Composable
//private fun Preview(
//    state: ScreenState = ScreenState.Loading
//) {
//
//    AppTheme {
//        KanjiInfoScreenUI(
//            char = PreviewKanji.kanji,
//            state = rememberUpdatedState(state),
//            onUpButtonClick = {},
//            onCopyButtonClick = {},
//            onFuriganaItemClick = {}
//        )
//    }
//
//}
//
//@Preview
//@Composable
//private fun NoDataPreview() {
//    Preview(ScreenState.NoData)
//}
//
//@Preview
//@Composable
//private fun KanaPreview() {
//    Preview(
//        state = ScreenState.Loaded.Kana(
//            character = "あ",
//            strokes = PreviewKanji.strokes,
//            radicals = emptyList(),
//            words = emptyList(),
//            kanaSystem = CharactersClassification.Kana.Hiragana,
//            reading = "A",
//        )
//    )
//}
//
//@Preview(locale = "ja")
//@Composable
//private fun KanjiPreview() {
//    Preview(
//        state = ScreenState.Loaded.Kanji(
//            character = PreviewKanji.kanji,
//            strokes = PreviewKanji.strokes,
//            radicals = PreviewKanji.radicals,
//            meanings = PreviewKanji.meanings,
//            on = PreviewKanji.on,
//            kun = PreviewKanji.kun,
//            grade = 1,
//            jlptLevel = 5,
//            frequency = 1,
//            words = PreviewKanji.randomWords(30),
//            wanikaniLevel = Random.nextInt(1, 60)
//        )
//    )
//}
//
//@Preview
//@Composable
//private fun ExpandedExpressionsPreview() {
//    AppTheme {
//        Surface {
//            LoadedState(
//                screenState = ScreenState.Loaded.Kana(
//                    character = "あ",
//                    strokes = PreviewKanji.strokes,
//                    radicals = PreviewKanji.radicals,
//                    words = PreviewKanji.randomWords(),
//                    kanaSystem = CharactersClassification.Kana.Hiragana,
//                    reading = "A"
//                ),
//                listState = rememberLazyListState(),
//                fabPosition = rememberUpdatedState(null),
//                onCopyButtonClick = {},
//                onFuriganaItemClick = {},
//                defaultRadicalsExpanded = false,
//                defaultWordsExpanded = true
//            )
//        }
//    }
//}
