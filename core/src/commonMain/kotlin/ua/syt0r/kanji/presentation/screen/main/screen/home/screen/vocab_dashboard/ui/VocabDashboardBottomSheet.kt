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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.core.srs.VocabDeckSrsProgress
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.neutralTextButtonColors
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.DashboardVocabDeck
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.BottomSheetState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabPracticePreviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType
import kotlin.math.roundToInt

@Composable
fun VocabDashboardBottomSheet(
    state: State<BottomSheetState?>,
    onEditClick: (DashboardVocabDeck) -> Unit,
    onDetailsClick: (DashboardVocabDeck) -> Unit,
    navigateToPractice: (MainDestination.VocabPractice) -> Unit
) {

    val density = LocalDensity.current
    var bottomSheetHeightDp by rememberSaveable { mutableStateOf(400) }

    val currentState = state.value
    if (currentState !is BottomSheetState.DeckSelected) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxWidth()
                .height(bottomSheetHeightDp.dp)
                .wrapContentSize()
        )
        return
    }

    var shouldShowSrsDialog by remember { mutableStateOf(false) }
    if (shouldShowSrsDialog) {
        VocabSrsDialog(
            onDismissRequest = { shouldShowSrsDialog = false },
            initial = currentState.srsPracticeType.value,
            onSelected = {
                currentState.srsPracticeType.value = it
                shouldShowSrsDialog = false
            }
        )
    }

    var selectedWord by remember { mutableStateOf<JapaneseWord?>(null) }
    selectedWord?.also {
        AlternativeWordsDialog(
            word = it,
            onDismissRequest = { selectedWord = null }
        )
    }

    val displaySrsProgress = currentState.deck.srsProgress.getValue(
        key = currentState.srsPracticeType.value
    )

    val wordsVisible = rememberSaveable(currentState.deck) { mutableStateOf(false) }

    Column(
        modifier = Modifier.heightIn(min = 400.dp)
            .onGloballyPositioned {
                if (it.isAttached) {
                    bottomSheetHeightDp = with(density) { it.size.height.toDp().value.roundToInt() }
                }
            }
    ) {

        ScreenBottomSheetHeader(
            deck = currentState.deck,
            srsProgress = displaySrsProgress,
            showWords = wordsVisible,
            onSrsConfigClick = { shouldShowSrsDialog = true },
            onEditClick = onEditClick,
            startPractice = { navigateToPractice(MainDestination.VocabPractice(it)) }
        )

        TextButton(
            onClick = { onDetailsClick(currentState.deck) },
            colors = ButtonDefaults.neutralTextButtonColors(),
        ) {
            Text("Details")
        }

        val wordsState = currentState.words.collectAsState()
        val wordsHidingOverlayAlpha = animateFloatAsState(
            targetValue = if (wordsVisible.value) 0f else 1f
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp)
        ) {

            when (val vocabPracticePreviewState = wordsState.value) {

                is VocabPracticePreviewState.Loaded -> {
                    itemsIndexed(vocabPracticePreviewState.words) { index, word ->
                        VocabItem(
                            listIndex = index,
                            word = word,
                            srsItemStatus = when {
                                displaySrsProgress.new.contains(word.id) -> SrsItemStatus.New
                                displaySrsProgress.due.contains(word.id) -> SrsItemStatus.Review
                                displaySrsProgress.done.contains(word.id) -> SrsItemStatus.Done
                                else -> {
                                    throw IllegalStateException("No srs status for word[${word.id}]")
                                }
                            },
                            overlayAlpha = wordsHidingOverlayAlpha,
                            onClick = { selectedWord = word }
                        )
                    }
                }

                VocabPracticePreviewState.Loading -> {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier.heightIn(max = 200.dp)
                                .fillParentMaxSize()
                                .wrapContentSize()
                        )
                    }
                }

            }

            item { Spacer(Modifier.height(20.dp)) }

        }

    }

}

@Composable
private fun ScreenBottomSheetHeader(
    deck: DashboardVocabDeck,
    srsProgress: VocabDeckSrsProgress,
    showWords: MutableState<Boolean>,
    onSrsConfigClick: () -> Unit,
    onEditClick: (DashboardVocabDeck) -> Unit,
    startPractice: (words: List<Long>) -> Unit
) {

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
                text = resolveString(deck.titleResolver),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { onEditClick(deck) }
            ) {
                Icon(
                    imageVector = when (deck is DashboardVocabDeck.Default) {
                        true -> Icons.AutoMirrored.Filled.PlaylistAdd
                        false -> Icons.Default.Edit
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

            IconButton(
                onClick = onSrsConfigClick
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .height(IntrinsicSize.Max)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val strings = resolveString { vocabDashboard }

            Text(
                text = strings.reviewLabel,
                modifier = Modifier.padding(vertical = 4.dp).alignByBaseline(),
                style = MaterialTheme.typography.labelLarge
            )

            SrsButton(
                color = MaterialTheme.extraColorScheme.new,
                text = strings.newWordsCounter(srsProgress.new.size),
                words = srsProgress.new,
                onClick = startPractice
            )
            SrsButton(
                color = MaterialTheme.extraColorScheme.due,
                text = strings.dueWordsCounter(srsProgress.due.size),
                words = srsProgress.due,
                onClick = startPractice
            )
            SrsButton(
                color = MaterialTheme.extraColorScheme.success,
                text = strings.doneWordsCounter(srsProgress.done.size),
                words = srsProgress.done,
                onClick = startPractice
            )
            SrsButton(
                color = MaterialTheme.colorScheme.outline,
                text = strings.totalWordsCounter(srsProgress.all.size),
                words = srsProgress.all,
                onClick = startPractice
            )

        }

    }
}

@Composable
private fun RowScope.SrsButton(
    color: Color,
    text: String,
    words: List<Long>,
    onClick: (List<Long>) -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.alignByBaseline()
            .fillMaxHeight()
            .clip(MaterialTheme.shapes.small)
            .clickable(
                enabled = words.isNotEmpty(),
                onClick = { onClick(words) }
            )
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
            text = text,
            fontWeight = FontWeight.Light,
            modifier = Modifier.alignByBaseline()
        )

    }
}

@Composable
private fun VocabItem(
    listIndex: Int,
    word: JapaneseWord,
    srsItemStatus: SrsItemStatus,
    overlayAlpha: State<Float>,
    onClick: () -> Unit
) {
    val hiddenColor = MaterialTheme.colorScheme.surfaceVariant
    val srsIndicatorColor = when (srsItemStatus) {
        SrsItemStatus.New -> MaterialTheme.extraColorScheme.new
        SrsItemStatus.Done -> MaterialTheme.extraColorScheme.success
        SrsItemStatus.Review -> MaterialTheme.extraColorScheme.due
    }
    Row(
        modifier = Modifier.height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Box(
            Modifier.width(6.dp).fillMaxHeight()
                .background(srsIndicatorColor, MaterialTheme.shapes.small)
        )

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
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

    }

}


@Composable
private fun VocabSrsDialog(
    onDismissRequest: () -> Unit,
    initial: VocabPracticeType,
    onSelected: (VocabPracticeType) -> Unit
) {

    val selected = remember { mutableStateOf(initial) }
    val strings = resolveString { vocabDashboard }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(strings.practiceTypeDialogTitle) },
        content = {
            Text(
                text = strings.practiceTypeDialogMessage,
                modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 10.dp)
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
                Text(strings.practiceTypeDialogCancelButton)
            }
            TextButton(onClick = { onSelected(selected.value) }) {
                Text(strings.practiceTypeDialogApplyButton)
            }
        },
        paddedContent = false
    )

}

@Composable
private fun SelectableRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(MaterialTheme.shapes.small)
            .let { if (selected) it.background(MaterialTheme.colorScheme.surfaceVariant) else it }
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
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
