package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.neutralTextButtonColors
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.ScreenState


@Composable
fun VocabDashboardScreenUI(
    state: State<ScreenState>,
    select: (DashboardVocabDeck) -> Unit,
    createDeck: () -> Unit,
    onEditClick: (DashboardVocabDeck) -> Unit,
    navigateToPractice: (DashboardVocabDeck) -> Unit
) {

    val extraListSpacerState = rememberExtraListSpacerState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = createDeck,
                modifier = Modifier.onGloballyPositioned { extraListSpacerState.updateOverlay(it) }
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) { screenState ->

            when (screenState) {
                ScreenState.Loading -> FancyLoading(Modifier.fillMaxSize().wrapContentSize())
                is ScreenState.Loaded -> ScreenLoadedState(
                    screenState = screenState,
                    extraListSpacerState = extraListSpacerState,
                    select = select,
                    onEditClick = onEditClick,
                    navigateToPractice = navigateToPractice
                )
            }

        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenLoadedState(
    screenState: ScreenState.Loaded,
    extraListSpacerState: ExtraListSpacerState,
    select: (DashboardVocabDeck) -> Unit,
    onEditClick: (DashboardVocabDeck) -> Unit,
    navigateToPractice: (DashboardVocabDeck) -> Unit
) {

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            tonalElevation = 0.dp
        ) {
            BottomSheetContent(
                state = screenState.deckSelectionState,
                onEditClick = onEditClick,
                navigateToPractice = navigateToPractice
            )
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { screenState.deckSelectionState.value }
            .filter { it == VocabDeckSelectionState.NothingSelected }
            .onEach { sheetState.hide() }
            .collect()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 20.dp)
            .fillMaxSize()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .onGloballyPositioned { extraListSpacerState.updateList(it) }
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(4.dp)) }


        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "User Decks",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (screenState.userDecks.isNotEmpty()) {
            items(screenState.userDecks) {
                PracticeGridItem(
                    title = resolveString(it.titleResolver),
                    onClick = {
                        select(it)
                        showBottomSheet = true
                    }
                )
            }
        } else {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "No decks saved. Use default decks to review vocabulary or create your own decks"
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) { Divider(Modifier.fillMaxWidth()) }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Default Decks",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(screenState.defaultDecks) { vocabPracticeSet ->
            PracticeGridItem(
                title = resolveString(vocabPracticeSet.titleResolver),
                onClick = {
                    select(vocabPracticeSet)
                    showBottomSheet = true
                }
            )
        }

        extraListSpacerState.ExtraSpacer(this)

    }

}

@Composable
private fun PracticeGridItem(
    title: String,
    onClick: () -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BottomSheetContent(
    state: State<VocabDeckSelectionState>,
    onEditClick: (DashboardVocabDeck) -> Unit,
    navigateToPractice: (DashboardVocabDeck) -> Unit
) {

    val currentState = state.value

    if (currentState == VocabDeckSelectionState.NothingSelected) {
        CircularProgressIndicator(Modifier.fillMaxWidth().wrapContentWidth())
        return
    }

    currentState as VocabDeckSelectionState.DeckSelected

    var selectedWord by remember { mutableStateOf<JapaneseWord?>(null) }
    selectedWord?.also {
        AlternativeWordsDialog(
            word = it,
            onDismissRequest = { selectedWord = null }
        )
    }

    val wordsState = currentState.words.collectAsState()
    val wordsVisible = rememberSaveable(currentState.deck.titleResolver) { mutableStateOf(false) }
    val wordsHidingOverlayAlpha = animateFloatAsState(
        targetValue = if (wordsVisible.value) 0f else 1f
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp)
            .heightIn(min = 400.dp),
    ) {

        item {
            ScreenBottomSheetHeader(
                selectionState = currentState,
                showWords = wordsVisible,
                onEditClick = onEditClick,
                onPracticeClick = navigateToPractice
            )
        }

        when (val vocabPracticePreviewState = wordsState.value) {

            is VocabPracticePreviewState.Loaded -> {
                itemsIndexed(vocabPracticePreviewState.words) { index, word ->
                    VocabItem(
                        listIndex = index,
                        word = word,
                        overlayAlpha = wordsHidingOverlayAlpha,
                        onClick = { selectedWord = word })
                }
            }

            VocabPracticePreviewState.Loading -> {
                item { CircularProgressIndicator(Modifier.size(24.dp)) }
            }

        }

        item { Spacer(Modifier.height(20.dp)) }

    }

}

@Composable
private fun ScreenBottomSheetHeader(
    selectionState: VocabDeckSelectionState.DeckSelected,
    showWords: MutableState<Boolean>,
    onEditClick: (DashboardVocabDeck) -> Unit,
    onPracticeClick: (DashboardVocabDeck) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Row(
            modifier = Modifier.padding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = resolveString(selectionState.deck.titleResolver),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { onEditClick(selectionState.deck) }
            ) {
                Icon(
                    imageVector = when (selectionState.deck.id) {
                        null -> Icons.AutoMirrored.Filled.PlaylistAdd
                        else -> Icons.Default.Edit
                    },
                    contentDescription = null
                )
            }

            IconButton(
                onClick = { showWords.value = !showWords.value }
            ) {
                Icon(
                    imageVector = when {
                        showWords.value -> Icons.Default.Visibility
                        else -> Icons.Default.VisibilityOff
                    },
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = { onPracticeClick(selectionState.deck) },
                colors = ButtonDefaults.neutralTextButtonColors()
            ) {
                Text(text = resolveString { vocabDashboard.reviewButton })
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            }
        }

        Text(
            text = resolveString { vocabDashboard.wordsCount(selectionState.deck.expressionIds.size) },
            style = MaterialTheme.typography.bodySmall
        )

    }
}

@Composable
private fun VocabItem(
    listIndex: Int,
    word: JapaneseWord,
    overlayAlpha: State<Float>,
    onClick: () -> Unit
) {
    val hiddenColor = MaterialTheme.colorScheme.surfaceVariant
    FuriganaText(
        furiganaString = word.orderedPreview(listIndex),
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = hiddenColor.copy(alpha = overlayAlpha.value),
                    size = size,
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
            .clickable(enabled = overlayAlpha.value == 0f, onClick = onClick)
            .padding(horizontal = 8.dp)
    )
}
