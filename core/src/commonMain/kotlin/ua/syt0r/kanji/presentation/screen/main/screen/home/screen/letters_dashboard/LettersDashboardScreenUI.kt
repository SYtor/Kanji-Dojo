package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Mediation
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.theme.customBlue
import ua.syt0r.kanji.presentation.common.theme.customOrange
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.FilledTextField
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.ui.LettersDashboardDailyLimitIndicator

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

    Scaffold(
        floatingActionButton = {
            val shouldShowButton by remember { derivedStateOf { state.value is ScreenState.Loaded } }
            AnimatedVisibility(
                visible = shouldShowButton,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(
                    onClick = navigateToDeckPicker,
                    modifier = Modifier.onGloballyPositioned { extraListSpacerState.updateOverlay(it) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
        },
        bottomBar = {
            LettersDashboardDailyLimitIndicator(
                state = remember {
                    derivedStateOf {
                        state.value.let { it as? ScreenState.Loaded }?.dailyIndicatorData
                    }
                },
                updateConfiguration = updateDailyGoalConfiguration
            )

        }
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(paddingValues)
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

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxSize()
            .wrapContentWidth()
            .onGloballyPositioned { extraListSpacerState.updateList(it) }
            .widthIn(max = 400.dp)
            .padding(horizontal = 10.dp)
    ) {

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

        ListItem(
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
            .padding(top = 4.dp)
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

@Composable
private fun MergeConfirmationDialog(
    listMode: LettersDashboardListMode.MergeMode,
    onDismissRequest: () -> Unit,
    onConfirmed: (LetterDecksMergeRequestData) -> Unit
) {
    MultiplatformDialog(onDismissRequest) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 20.dp, bottom = 10.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            val title = listMode.title.value
            val decksIdList = listMode.selected.value.toList()
            val mergedDeckTitles = listMode.items
                .filter { decksIdList.contains(it.deckId) }
                .map { it.title }

            val strings = resolveString { lettersDashboard }

            Text(
                text = strings.mergeDialogTitle,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = strings.mergeDialogMessage(title, mergedDeckTitles),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(strings.mergeDialogCancelButton)
                }
                TextButton(
                    onClick = {
                        onConfirmed(
                            LetterDecksMergeRequestData(
                                title = listMode.title.value,
                                deckIds = listMode.selected.value.toList()
                            )
                        )
                    }
                ) {
                    Text(strings.mergeDialogAcceptButton)
                }
            }

        }

    }
}

@Composable
private fun EmptyState() {
    Text(
        text = buildAnnotatedString {
            append("Create deck by clicking on ")
            appendInlineContent("icon")
            append(" button. Decks are used to track your progress")
        },
        inlineContent = mapOf(
            "icon" to InlineTextContent(
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

@Composable
private fun ListItem(
    item: LettersDashboardItem,
    dailyGoalEnabled: Boolean,
    onItemClick: () -> Unit,
    quickPractice: (MainDestination.Practice) -> Unit
) {

    var expanded by rememberSaveable(item.deckId) { mutableStateOf(false) }

    Column(
        modifier = Modifier.clip(MaterialTheme.shapes.large)
    ) {

        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = { expanded = !expanded })
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Column(
                modifier = Modifier.weight(1f)
                    .padding(start = 10.dp)
                    .padding(vertical = 10.dp),
            ) {

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = resolveString {
                        lettersDashboard.itemTimeMessage(item.timeSinceLastReview)
                    },
                    style = MaterialTheme.typography.bodySmall,
                )

            }

            if (dailyGoalEnabled) {
                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                    DeckSneakPeekIndicator(
                        icon = Icons.Default.Draw,
                        study = item.writingProgress.quickLearn.size,
                        review = item.writingProgress.quickReview.size
                    )
                    DeckSneakPeekIndicator(
                        icon = Icons.Default.LocalLibrary,
                        study = item.readingProgress.quickLearn.size,
                        review = item.readingProgress.quickReview.size
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(onClick = onItemClick)
                    .padding(horizontal = 20.dp)
                    .wrapContentSize()
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            }

        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            ListItemDetails(item, quickPractice)
        }

    }

}

@Composable
private fun DeckSneakPeekIndicator(icon: ImageVector, study: Int, review: Int) {
    if (study == 0 && review == 0) return
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        if (study > 0) {
            Box(
                modifier = Modifier.align(Alignment.CenterVertically).size(4.dp)
                    .background(customBlue, CircleShape)
            )
        }
        if (review > 0) {
            Box(
                modifier = Modifier.align(Alignment.CenterVertically).size(4.dp)
                    .background(customOrange, CircleShape)
            )
        }
    }
}

@Composable
private fun ListItemDetails(
    data: LettersDashboardItem,
    quickPractice: (MainDestination.Practice) -> Unit
) {

    val strings = resolveString { lettersDashboard }

    val isReadingMode = rememberSaveable(data.deckId) { mutableStateOf(false) }
    val studyProgress by remember {
        derivedStateOf { if (isReadingMode.value) data.readingProgress else data.writingProgress }
    }

    val onQuickPracticeButtonClick: (characters: List<String>) -> Unit = lambda@{
        if (it.isEmpty()) return@lambda
        val destination = when (isReadingMode.value) {
            true -> MainDestination.Practice.Reading(data.deckId, it)
            false -> MainDestination.Practice.Writing(data.deckId, it)
        }
        quickPractice(destination)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {

        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Bottom
        ) {

            Column(
                modifier = Modifier.weight(1f).fillMaxSize()
            ) {

                PracticeTypeSwitch(isReadingMode = isReadingMode)

                IndicatorTextRow(
                    color = MaterialTheme.colorScheme.outline,
                    label = strings.itemTotal,
                    characters = studyProgress.all,
                    onClick = onQuickPracticeButtonClick
                )

                IndicatorTextRow(
                    color = MaterialTheme.extraColorScheme.success,
                    label = strings.itemDone,
                    characters = studyProgress.known,
                    onClick = onQuickPracticeButtonClick
                )

                IndicatorTextRow(
                    color = customOrange,
                    label = strings.itemReview,
                    characters = studyProgress.review,
                    onClick = onQuickPracticeButtonClick
                )

                IndicatorTextRow(
                    color = customBlue,
                    label = strings.itemNew,
                    characters = studyProgress.new,
                    onClick = onQuickPracticeButtonClick
                )

            }

            Box(
                modifier = Modifier.size(120.dp)
            ) {

                PieIndicator(
                    max = studyProgress.all.size.toFloat(),
                    known = animateFloatAsState(targetValue = studyProgress.known.size.toFloat()),
                    review = animateFloatAsState(targetValue = studyProgress.review.size.toFloat()),
                    new = animateFloatAsState(targetValue = studyProgress.new.size.toFloat()),
                    modifier = Modifier.fillMaxSize()
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Light,
                                fontSize = 14.textDp,
                            )
                        ) { append(strings.itemGraphProgressTitle) }
                        append("\n")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 22.textDp
                            )
                        ) { append(strings.itemGraphProgressValue(studyProgress.completionPercentage)) }
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )

            }

        }

        Text(text = strings.itemQuickPracticeTitle, style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            QuickPracticeButton(
                enabled = studyProgress.quickLearn.isNotEmpty(),
                text = strings.itemQuickPracticeLearn(studyProgress.quickLearn.size),
                onClick = { onQuickPracticeButtonClick(studyProgress.quickLearn) }
            )

            QuickPracticeButton(
                enabled = studyProgress.quickReview.isNotEmpty(),
                text = strings.itemQuickPracticeReview(studyProgress.quickReview.size),
                onClick = { onQuickPracticeButtonClick(studyProgress.quickReview) }
            )

        }

    }

}

@Composable
private fun ColumnScope.IndicatorTextRow(
    color: Color,
    label: String,
    characters: List<String>,
    onClick: (List<String>) -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth(fraction = 0.8f)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = { onClick(characters) })
            .padding(horizontal = 10.dp)
    ) {

        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )

        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                    )
                ) { append(label) }
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 22.sp
                    )
                ) { append(" ${characters.size}") }
            },
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = { onClick(characters) },
            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.End).size(20.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }

    }

}

@Composable
private fun PieIndicator(
    max: Float,
    known: State<Float>,
    review: State<Float>,
    new: State<Float>,
    modifier: Modifier = Modifier,
) {

    val knownColor = MaterialTheme.extraColorScheme.success

    Canvas(
        modifier = modifier
    ) {

        val strokeWidth = 10.dp.toPx()
        val strokeStyle = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        )
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val arcOffset = Offset(strokeWidth, strokeWidth).div(2f)

        if (max == 0f) {
            drawArc(
                size = arcSize,
                topLeft = arcOffset,
                color = knownColor,
                startAngle = 270f,
                sweepAngle = 360f,
                useCenter = false,
                style = strokeStyle
            )
            return@Canvas
        }

        val strokeParts = listOf(
            knownColor to known.value,
            customOrange to review.value,
            customBlue to new.value,
        )

        var accumulatedAngle = 0f
        strokeParts.forEach { (color, value) ->
            drawArc(
                size = arcSize,
                topLeft = arcOffset,
                color = color,
                startAngle = 270f + accumulatedAngle / max * 360,
                sweepAngle = value / max * 360,
                useCenter = false,
                style = strokeStyle
            )
            accumulatedAngle += value
        }

    }
}

@Composable
private fun ColumnScope.PracticeTypeSwitch(
    isReadingMode: MutableState<Boolean>
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.align(Alignment.Start)
    ) {

        Switch(
            checked = isReadingMode.value,
            onCheckedChange = { isReadingMode.value = !isReadingMode.value },
            thumbContent = {
                val icon = if (isReadingMode.value) Icons.Default.LocalLibrary
                else Icons.Default.Draw
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.outline,
                checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                checkedIconColor = MaterialTheme.colorScheme.surfaceVariant,
                checkedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedIconColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        )

        Text(
            text = resolveString {
                if (isReadingMode.value) lettersDashboard.itemReadingTitle else lettersDashboard.itemWritingTitle
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraLight
        )

    }

}

@Composable
private fun RowScope.QuickPracticeButton(
    enabled: Boolean,
    text: String,
    onClick: () -> Unit
) {

    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        enabled = enabled
    ) {
        Text(text)
    }

}
