package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.CollapsibleContainer
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.rememberCollapsibleContainerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.customBlue
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.DashboardVocabDeck
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDeckSelectionState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabPracticePreviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType

@Composable
fun VocabDashboardBottomSheet(
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

    Column(
        modifier = Modifier.heightIn(min = 400.dp)
    ) {

        val collapsibleContainerState = rememberCollapsibleContainerState()
        CollapsibleContainer(collapsibleContainerState) {
            ScreenBottomSheetHeader(
                selectionState = currentState,
                showWords = wordsVisible,
                onEditClick = onEditClick,
                onPracticeClick = { navigateToPractice(currentState.deck) }
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
                .nestedScroll(collapsibleContainerState.nestedScrollConnection)
                .padding(horizontal = 20.dp)
        ) {

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

}

@Composable
private fun ScreenBottomSheetHeader(
    selectionState: VocabDeckSelectionState.DeckSelected,
    showWords: MutableState<Boolean>,
    onEditClick: (DashboardVocabDeck) -> Unit,
    onPracticeClick: () -> Unit
) {

    var shouldShowSrsDialog by remember { mutableStateOf(false) }
    if (shouldShowSrsDialog) {
        VocabSrsDialog(
            onDismissRequest = { shouldShowSrsDialog = false }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
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

        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .height(IntrinsicSize.Max)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
                    .aspectRatio(1f, true)
                    .wrapContentSize()
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { shouldShowSrsDialog = true }
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Review Options:",
                modifier = Modifier.padding(vertical = 4.dp).alignByBaseline(),
                style = MaterialTheme.typography.labelLarge
            )

            SrsButton(
                color = MaterialTheme.colorScheme.outline,
                label = "All",
                count = 10,
                onClick = onPracticeClick
            )
            SrsButton(
                color = MaterialTheme.extraColorScheme.success,
                label = "Done",
                count = 10,
                onClick = onPracticeClick
            )
            SrsButton(
                color = MaterialTheme.extraColorScheme.outdated,
                label = "Due",
                count = 10,
                onClick = onPracticeClick
            )
            SrsButton(
                color = customBlue,
                label = "New",
                count = 10,
                onClick = onPracticeClick
            )

        }

    }
}

@Composable
private fun RowScope.SrsButton(
    color: Color,
    label: String,
    count: Int,
    onClick: () -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.alignByBaseline()
            .fillMaxHeight()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {

        Box(
            modifier = Modifier
                .alignBy { it.measuredHeight }
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )

        Text(
            text = "$label: $count",
            fontWeight = FontWeight.Light,
            modifier = Modifier.alignByBaseline()
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


@Composable
private fun VocabSrsDialog(
    onDismissRequest: () -> Unit
) {

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("SRS Practice Type") },
        content = {
            val selected = remember { mutableStateOf(VocabPracticeType.Flashcard) }
            Text(
                text = "Select practice type used to display review statuses",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.labelSmall
            )
            VocabPracticeType.values().forEach {
                SelectableRow(
                    title = resolveString(it.titleResolver),
                    selected = selected.value == it,
                    onClick = { selected.value = it }
                )
            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
            TextButton(onClick = onDismissRequest) {
                Text("Apply")
            }
        },
        paddedContent = false
    )

}

@Composable
private fun SelectableRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 10.dp)
            .clip(MaterialTheme.shapes.small)
            .let { if (selected) it.background(MaterialTheme.colorScheme.surfaceVariant) else it }
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = title,
            modifier = Modifier.weight(1f).alignByBaseline()
        )

        val extraIconPadding = with(LocalDensity.current) { 5.dp.roundToPx() }

        if (selected)
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(24.dp)
                    .alignBy { it.measuredHeight - extraIconPadding }
            )

    }
}
