package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.FilledTextField
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.ui.LetterDashboardBottomBarUI
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.ui.LetterDashboardListItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.ui.MergeConfirmationDialog

@Composable
fun LettersDashboardScreenUI(
    state: State<ScreenState>,
    startMerge: () -> Unit,
    merge: (LetterDecksMergeRequestData) -> Unit,
    startReorder: () -> Unit,
    reorder: (LetterDecksReorderRequestData) -> Unit,
    enableDefaultMode: () -> Unit,
    navigateToDeckDetails: (LettersDashboardItem) -> Unit,
    startQuickPractice: (MainDestination.Practice) -> Unit,
    updateDailyGoalConfiguration: (DailyGoalConfiguration) -> Unit,
    navigateToDeckPicker: () -> Unit
) {

    val extraListSpacerState = rememberExtraListSpacerState()

    Box {

        AnimatedContent(
            targetState = state.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
        ) { screenState ->
            when (screenState) {
                ScreenState.Loading -> {
                    LoadingState()
                }

                is ScreenState.Loaded -> {
                    val mode = screenState.mode.collectAsState()
                    val isEmpty by remember { derivedStateOf { mode.value.items.isEmpty() } }
                    if (isEmpty) {
                        EmptyState()
                    } else {
                        LoadedState(
                            listMode = mode,
                            dailyIndicatorData = screenState.dailyIndicatorData,
                            extraListSpacerState = extraListSpacerState,
                            startMerge = startMerge,
                            merge = merge,
                            startReorder = startReorder,
                            reorder = reorder,
                            enableDefaultMode = enableDefaultMode,
                            onDetailsClick = navigateToDeckDetails,
                            startQuickPractice = startQuickPractice
                        )
                    }
                }
            }
        }

        LetterDashboardBottomBarUI(
            state = state,
            navigateToDeckPicker = navigateToDeckPicker,
            updateConfiguration = updateDailyGoalConfiguration,
            modifier = Modifier.align(Alignment.BottomCenter)
                .onGloballyPositioned { extraListSpacerState.updateOverlay(it) },
        )

    }

}

@Composable
private fun LoadingState() {
    FancyLoading(Modifier.fillMaxSize().wrapContentSize())
}

@Composable
private fun LoadedState(
    listMode: State<LettersDashboardListMode>,
    dailyIndicatorData: DailyIndicatorData,
    extraListSpacerState: ExtraListSpacerState,
    startMerge: () -> Unit,
    merge: (LetterDecksMergeRequestData) -> Unit,
    startReorder: () -> Unit,
    reorder: (LetterDecksReorderRequestData) -> Unit,
    enableDefaultMode: () -> Unit,
    onDetailsClick: (LettersDashboardItem) -> Unit,
    startQuickPractice: (MainDestination.Practice) -> Unit
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

        val currentMode = listMode.value

        if (currentMode.items.size > 1) {
            item {
                ListModeButtons(
                    listMode = listMode,
                    startMerge = startMerge,
                    merge = merge,
                    startReorder = startReorder,
                    reorder = reorder,
                    enableDefaultMode = enableDefaultMode
                )
            }
        }

        when (currentMode) {
            is LettersDashboardListMode.Default -> {
                addContent(currentMode, dailyIndicatorData, onDetailsClick, startQuickPractice)
            }

            is LettersDashboardListMode.MergeMode -> {
                addContent(currentMode)
            }

            is LettersDashboardListMode.SortMode -> {
                addContent(currentMode)
            }
        }

        item { extraListSpacerState.ExtraSpacer() }

    }

}

private fun LazyListScope.addContent(
    listMode: LettersDashboardListMode.Default,
    dailyIndicatorData: DailyIndicatorData,
    onDetailsClick: (LettersDashboardItem) -> Unit,
    startQuickPractice: (MainDestination.Practice) -> Unit
) {

    items(
        items = listMode.items,
        key = { listMode::class.simpleName to it.deckId }
    ) {

        LetterDashboardListItem(
            item = it,
            dailyGoalEnabled = dailyIndicatorData.configuration.enabled,
            onItemClick = { onDetailsClick(it) },
            quickPractice = startQuickPractice
        )

    }

}

private fun LazyListScope.addContent(
    listMode: LettersDashboardListMode.MergeMode
) {

    item {
        Column(
            modifier = Modifier,
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
                value = listMode.title.value,
                onValueChange = { listMode.title.value = it },
                modifier = Modifier.fillMaxWidth(),
                hintContent = {
                    Text(
                        text = strings.mergeTitleHint,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = strings.mergeSelectedCount(listMode.selected.value.size),
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { listMode.selected.value = emptySet() },
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

    items(
        items = listMode.items,
        key = { listMode::class.simpleName to it.deckId }
    ) {
        val isSelected = listMode.selected.value.contains(it.deckId)
        val onClick = {
            listMode.selected.value = listMode.selected.value.run {
                if (isSelected) minus(it.deckId)
                else plus(it.deckId)
            }
        }
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(start = 10.dp),
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
private fun LazyListScope.addContent(
    listMode: LettersDashboardListMode.SortMode
) {

    item {
        Text(
            text = resolveString { lettersDashboard.sortTitle },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth().wrapContentSize()
        )
    }

    item {
        val toggleSwitchValue = {
            listMode.sortByReviewTime.value = listMode.sortByReviewTime.value.not()
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clip(MaterialTheme.shapes.large)
                .clickable(onClick = toggleSwitchValue)
                .padding(horizontal = 10.dp)
        ) {
            Text(
                text = resolveString { lettersDashboard.sortByTimeTitle },
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = listMode.sortByReviewTime.value,
                onCheckedChange = { toggleSwitchValue() }
            )
        }
    }

    val sortEnabled = !listMode.sortByReviewTime.value

    itemsIndexed(
        items = listMode.reorderedList.value,
        key = { _, item -> listMode::class.simpleName to item.deckId }
    ) { index, item ->
        Row(
            modifier = Modifier
                .animateItemPlacement()
                .clip(MaterialTheme.shapes.large)
                .fillMaxWidth()
                .padding(start = 10.dp),
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
                        val currentList = listMode.reorderedList.value
                        if (index == currentList.size - 1) return@IconButton
                        listMode.reorderedList.value = currentList.withSwappedItems(
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
                        val currentList = listMode.reorderedList.value
                        listMode.reorderedList.value = currentList.withSwappedItems(
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

@Composable
private fun ListModeButtons(
    listMode: State<LettersDashboardListMode>,
    startMerge: () -> Unit,
    merge: (LetterDecksMergeRequestData) -> Unit,
    startReorder: () -> Unit,
    reorder: (LetterDecksReorderRequestData) -> Unit,
    enableDefaultMode: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(MaterialTheme.shapes.large)
    ) {
        AnimatedContent(
            targetState = listMode.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) {
            val strings = resolveString { lettersDashboard }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                when (it) {
                    is LettersDashboardListMode.Default -> {
                        OptionButton(
                            title = strings.mergeButton,
                            icon = Icons.Default.Mediation,
                            onClick = startMerge,
                            alignment = Alignment.Start
                        )
                        OptionButton(
                            title = strings.sortButton,
                            icon = Icons.AutoMirrored.Filled.Sort,
                            onClick = startReorder,
                            alignment = Alignment.End
                        )
                    }

                    is LettersDashboardListMode.MergeMode -> {
                        val showConfirmationDialog = remember { mutableStateOf(false) }
                        if (showConfirmationDialog.value) {
                            MergeConfirmationDialog(
                                listMode = it,
                                onDismissRequest = { showConfirmationDialog.value = false },
                                onConfirmed = merge
                            )
                        }
                        OptionButton(
                            title = strings.mergeCancelButton,
                            icon = Icons.Default.Clear,
                            onClick = enableDefaultMode,
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

                    is LettersDashboardListMode.SortMode -> {
                        OptionButton(
                            title = strings.sortCancelButton,
                            icon = Icons.Default.Clear,
                            onClick = enableDefaultMode,
                            alignment = Alignment.Start
                        )
                        OptionButton(
                            title = strings.sortAcceptButton,
                            icon = Icons.Default.Check,
                            onClick = {
                                reorder(
                                    LetterDecksReorderRequestData(
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
private fun EmptyState() {
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
