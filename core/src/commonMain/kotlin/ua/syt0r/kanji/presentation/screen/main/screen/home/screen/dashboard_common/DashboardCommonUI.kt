package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mediation
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.ui.FilledTextField
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation


@Composable
fun DeckDashboardLoadedStateContainer(
    extraListSpacerState: ExtraListSpacerState,
    content: LazyListScope.() -> Unit
) {

    val orientation = LocalOrientation.current

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxSize()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .onGloballyPositioned { extraListSpacerState.updateList(it) }
    ) {

        if (orientation == Orientation.Landscape) {
            item { Spacer(Modifier.height(20.dp)) }
        }

        content()

        item { extraListSpacerState.ExtraSpacer() }

    }

}

fun DeckDashboardListState.addMergeItems(
    scope: LazyListScope,
    listMode: DeckDashboardListMode.MergeMode,
) = scope.apply {

    var title by listMode.title
    var selected by listMode.selected

    item {

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val strings = resolveString { lettersDashboard }
            Text(
                text = strings.mergeTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            FilledTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                hintContent = {
                    Text(
                        text = strings.mergeTitleHint,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = strings.mergeSelectedCount(selected.size),
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { selected = emptySet() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(strings.mergeClearSelectionButton)
                    Icon(Icons.Default.Clear, null)
                }
            }

        }

    }

    val listModeKey = DeckDashboardListMode.MergeMode::class.simpleName

    items(
        items = items,
        key = { listModeKey to it.id }
    ) {
        val isSelected = selected.contains(it.id)
        val onClick = { selected = selected.run { if (isSelected) minus(it.id) else plus(it.id) } }
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = it.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
fun DeckDashboardListState.addSortItems(
    scope: LazyListScope,
    listMode: DeckDashboardListMode.SortMode
) = scope.apply {

    var reorderedList by listMode.reorderedList
    var sortByReviewTime by listMode.sortByReviewTime

    item {
        Text(
            text = resolveString { lettersDashboard.sortTitle },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 36.dp)
                .wrapContentSize()
        )
    }

    item {
        val toggleSwitchValue = { sortByReviewTime = !sortByReviewTime }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = toggleSwitchValue)
                .padding(start = 16.dp, end = 8.dp)
        ) {
            Text(
                text = resolveString { lettersDashboard.sortByTimeTitle },
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = sortByReviewTime,
                onCheckedChange = { toggleSwitchValue() }
            )
        }
    }

    val sortEnabled = !sortByReviewTime
    val listModeKey = DeckDashboardListMode.SortMode::class.simpleName

    itemsIndexed(
        items = reorderedList,
        key = { _, item -> listModeKey to item.id }
    ) { index, item ->
        Row(
            modifier = Modifier
                .animateItemPlacement()
                .clip(MaterialTheme.shapes.large)
                .fillMaxWidth()
                .padding(start = 36.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(
                    onClick = {
                        val currentList = reorderedList
                        if (index == currentList.size - 1) return@IconButton
                        reorderedList = currentList.withSwappedItems(
                            index1 = index,
                            index2 = index + 1
                        )
                    },
                    enabled = sortEnabled
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, null)
                }
                IconButton(
                    onClick = {
                        if (index == 0) return@IconButton
                        val currentList = reorderedList
                        reorderedList = currentList.withSwappedItems(
                            index1 = index,
                            index2 = index - 1
                        )
                    },
                    enabled = sortEnabled
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, null)
                }
            }
        }
    }

}

fun <T> List<T>.withSwappedItems(index1: Int, index2: Int): List<T> {
    val item1 = get(index1)
    val item2 = get(index2)
    return mapIndexed { index, item ->
        when (index) {
            index1 -> item2
            index2 -> item1
            else -> item
        }
    }
}

fun LazyListScope.deckDashboardListModeButtons(
    listState: DeckDashboardListState,
    mergeDecks: (DecksMergeRequestData) -> Unit,
    sortDecks: (DecksSortRequestData) -> Unit,
) = item {

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(MaterialTheme.shapes.large)
    ) {
        AnimatedContent(
            targetState = listState.mode.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) {
            val strings = resolveString { lettersDashboard }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                when (it) {
                    is DeckDashboardListMode.Browsing -> {
                        OptionButton(
                            title = strings.mergeButton,
                            icon = Icons.Default.Mediation,
                            onClick = {
                                listState.mode.value = DeckDashboardListMode.MergeMode(
                                    selected = mutableStateOf(emptySet()),
                                    title = mutableStateOf("")
                                )
                            },
                            alignment = Alignment.Start
                        )
                        OptionButton(
                            title = strings.sortButton,
                            icon = Icons.AutoMirrored.Filled.Sort,
                            onClick = {
                                listState.mode.value = DeckDashboardListMode.SortMode(
                                    reorderedList = mutableStateOf(listState.items),
                                    sortByReviewTime = mutableStateOf(listState.appliedSortByReviewTime.value)
                                )
                            },
                            alignment = Alignment.End
                        )
                    }

                    is DeckDashboardListMode.MergeMode -> {
                        val showConfirmationDialog = remember { mutableStateOf(false) }
                        if (showConfirmationDialog.value) {
                            MergeConfirmationDialog(
                                decks = listState.items,
                                listMode = it,
                                onDismissRequest = { showConfirmationDialog.value = false },
                                onConfirmed = mergeDecks
                            )
                        }
                        OptionButton(
                            title = strings.mergeCancelButton,
                            icon = Icons.Default.Clear,
                            onClick = { listState.mode.value = DeckDashboardListMode.Browsing },
                            alignment = Alignment.Start
                        )
                        val mergeButtonEnabled = remember {
                            derivedStateOf {
                                it.title.value.isNotEmpty() &&
                                        it.selected.value.size > 1
                            }
                        }
                        OptionButton(
                            title = strings.mergeAcceptButton,
                            icon = Icons.Default.Check,
                            onClick = { showConfirmationDialog.value = true },
                            alignment = Alignment.End,
                            enabled = mergeButtonEnabled.value
                        )
                    }

                    is DeckDashboardListMode.SortMode -> {
                        OptionButton(
                            title = strings.sortCancelButton,
                            icon = Icons.Default.Clear,
                            onClick = { listState.mode.value = DeckDashboardListMode.Browsing },
                            alignment = Alignment.Start
                        )
                        OptionButton(
                            title = strings.sortAcceptButton,
                            icon = Icons.Default.Check,
                            onClick = {
                                listState.appliedSortByReviewTime.value = it.sortByReviewTime.value
                                sortDecks(
                                    DecksSortRequestData(
                                        reorderedList = it.reorderedList.value,
                                        sortByTime = it.sortByReviewTime.value
                                    )
                                )
                            },
                            alignment = Alignment.End
                        )
                    }
                }
            }
        }
    }
}

private val modeButtonCornerRadius = 12.dp

@Composable
private fun RowScope.OptionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    alignment: Alignment.Horizontal,
    enabled: Boolean = true
) {
    val buttonShape = when (alignment) {
        Alignment.End -> RoundedCornerShape(
            topEnd = modeButtonCornerRadius,
            bottomEnd = modeButtonCornerRadius
        )

        Alignment.Start -> RoundedCornerShape(
            topStart = modeButtonCornerRadius,
            bottomStart = modeButtonCornerRadius
        )

        else -> throw IllegalStateException("Unsupported alignment")
    }
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = buttonShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterVertically)
                .padding(end = 8.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

private const val InlineIconId = "icon"

@Composable
fun DeckDashboardEmptyState() {
    Text(
        text = resolveString { lettersDashboard.emptyScreenMessage(InlineIconId) },
        inlineContent = mapOf(
            InlineIconId to InlineTextContent(
                placeholder = Placeholder(
                    width = 24.textDp,
                    height = 24.textDp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                ),
                children = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.shapes.small
                            )
                            .padding(4.dp)
                    )
                }
            )
        ),
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp),
        textAlign = TextAlign.Center
    )
}
