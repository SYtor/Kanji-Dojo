package ua.syt0r.kanji.presentation.screen.main.screen.deck_details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.RadioButtonChecked
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.DeckDetailsScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsListItem
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsVisibleData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.FilterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui.DeckDetailsBottomSheet
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui.DeckDetailsFilterDialog
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui.DeckDetailsGroupsUI
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui.DeckDetailsItemsUI
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui.DeckDetailsLayoutDialog
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui.DeckDetailsSortDialog
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui.DeckDetailsToolbar
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui.DeckDetailsVocabUI

@Composable
fun DeckDetailsScreenUI(
    state: State<ScreenState>,
    navigateUp: () -> Unit,
    navigateToDeckEdit: () -> Unit,
    navigateToCharacterDetails: (String) -> Unit,
    startGroupReview: (DeckDetailsListItem.Group) -> Unit,
    startMultiselectReview: () -> Unit,
) {

    var shouldShowVisibilityDialog by remember { mutableStateOf(false) }
    if (shouldShowVisibilityDialog) {
        val loadedState = state.value.let { it as ScreenState.Loaded.Letters }
        val configuration = loadedState.configuration.value
        DeckDetailsLayoutDialog(
            layout = configuration.layout,
            kanaGroups = configuration.kanaGroups,
            onDismissRequest = { shouldShowVisibilityDialog = false },
            onApplyConfiguration = { layout, kanaGroups ->
                shouldShowVisibilityDialog = false
                loadedState.configuration.value = configuration.copy(
                    layout = layout,
                    kanaGroups = kanaGroups
                )
            }
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val practiceSharer = rememberPracticeSharer(snackbarHostState)
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    if (bottomSheetState.isVisible) {
        MultiplatformBackHandler { coroutineScope.launch { bottomSheetState.hide() } }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        modifier = Modifier.clipToBounds(),
        sheetBackgroundColor = MaterialTheme.colorScheme.surface,
        sheetShape = MaterialTheme.shapes.large.copy(
            bottomStart = ZeroCornerSize,
            bottomEnd = ZeroCornerSize
        ),
        sheetContent = {
            DeckDetailsBottomSheet(
                state = state,
                onCharacterClick = navigateToCharacterDetails,
                onStudyClick = startGroupReview,
                onDismissRequest = { bottomSheetState.hide() }
            )
        }
    ) {

        Scaffold(
            topBar = {
                DeckDetailsToolbar(
                    state = state,
                    upButtonClick = navigateUp,
                    onVisibilityButtonClick = { shouldShowVisibilityDialog = true },
                    editButtonClick = navigateToDeckEdit,
                    shareButtonClick = { practiceSharer.share(it) }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->

            val noGroupsSelectedMessage = resolveString { deckDetails.multiselectNoSelected }

            ScreenContent(
                state = state,
                navigateToCharacterDetails = navigateToCharacterDetails,
                showGroupSheet = {
                    coroutineScope.launch { bottomSheetState.show() }
                },
                startMultiselectReview = startMultiselectReview,
                showNoSelectionMessage = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = noGroupsSelectedMessage,
                            withDismissAction = true
                        )
                    }
                },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            )

        }

    }

}

@Composable
private fun ScreenContent(
    state: State<ScreenState>,
    navigateToCharacterDetails: (String) -> Unit,
    showGroupSheet: () -> Unit,
    startMultiselectReview: () -> Unit,
    showNoSelectionMessage: () -> Unit,
    modifier: Modifier,
) {

    val extraListSpacerState = rememberExtraListSpacerState()

    val transition = updateTransition(targetState = state.value, label = "State Transition")
    transition.AnimatedContent(
        contentKey = { it::class },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.onGloballyPositioned { extraListSpacerState.updateList(it) }
    ) { screenState ->

        when (screenState) {
            ScreenState.Loading -> {
                FancyLoading(Modifier.fillMaxSize().wrapContentSize())
            }

            is ScreenState.Loaded -> {
                ScreenLoadedState(
                    screenState = screenState,
                    extraListSpacerState = extraListSpacerState,
                    onCharacterClick = navigateToCharacterDetails,
                    showGroupSheet = showGroupSheet,
                    toggleItemSelection = { it.selected.run { value = !value } }
                )

                if (screenState.isSelectionModeEnabled.value) {
                    MultiplatformBackHandler(
                        onBack = { screenState.isSelectionModeEnabled.value = false }
                    )
                }
            }
        }

    }

    transition.AnimatedContent(
        transitionSpec = { scaleIn() togetherWith scaleOut() },
        modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.BottomEnd),
        contentAlignment = Alignment.BottomEnd
    ) { screenState ->
        if (screenState !is ScreenState.Loaded) {
            Box(Modifier)
            return@AnimatedContent
        }


        FAB(
            screenState = screenState,
            extraListSpacerState = extraListSpacerState,
            startPractice = {
                val anyItemSelected = screenState.visibleDataState.value
                    .items.any { it.selected.value }
                if (anyItemSelected) {
                    startMultiselectReview()
                } else {
                    showNoSelectionMessage()
                }
            },
            startSelectionMode = { screenState.isSelectionModeEnabled.apply { value = !value } }
        )
    }

}

@Composable
private fun ScreenLoadedState(
    screenState: ScreenState.Loaded,
    extraListSpacerState: ExtraListSpacerState,
    onCharacterClick: (String) -> Unit,
    showGroupSheet: () -> Unit,
    toggleItemSelection: (DeckDetailsListItem) -> Unit,
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AnimatedContent(
            targetState = screenState.visibleDataState.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.fillMaxSize()
        ) { visibleData ->

            when (visibleData) {
                is DeckDetailsVisibleData.Groups -> {
                    screenState as ScreenState.Loaded.Letters
                    DeckDetailsGroupsUI(
                        configuration = screenState.configuration.value,
                        selectionModeEnabled = screenState.isSelectionModeEnabled,
                        visibleData = visibleData,
                        extraListSpacerState = extraListSpacerState,
                        onConfigurationUpdate = { screenState.configuration.value = it },
                        selectGroup = {
                            visibleData.selectedItem.value = it
                            showGroupSheet()
                        },
                        toggleGroupSelection = toggleItemSelection
                    )
                }

                is DeckDetailsVisibleData.Items -> {
                    screenState as ScreenState.Loaded.Letters
                    DeckDetailsItemsUI(
                        configuration = screenState.configuration.value,
                        selectionModeEnabled = screenState.isSelectionModeEnabled,
                        visibleData = visibleData,
                        extraListSpacerState = extraListSpacerState,
                        onConfigurationUpdate = { screenState.configuration.value = it },
                        onCharacterClick = onCharacterClick,
                        onSelectionToggled = toggleItemSelection
                    )
                }

                is DeckDetailsVisibleData.Vocab -> {
                    screenState as ScreenState.Loaded.Vocab
                    DeckDetailsVocabUI(
                        screenState = screenState,
                        visibleData = visibleData,
                        extraListSpacerState = extraListSpacerState,
                        onConfigurationUpdate = { screenState.configuration.value = it },
                        toggleItemSelection = toggleItemSelection
                    )
                }
            }

        }

    }

}

@Composable
fun SrsItemStatus.toColor(): Color = when (this) {
    SrsItemStatus.Done -> MaterialTheme.extraColorScheme.success
    SrsItemStatus.Review -> MaterialTheme.extraColorScheme.due
    else -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun FAB(
    screenState: ScreenState.Loaded,
    extraListSpacerState: ExtraListSpacerState,
    startPractice: () -> Unit,
    startSelectionMode: () -> Unit,
) {

    FloatingActionButton(
        onClick = {
            if (screenState.isSelectionModeEnabled.value) {
                startPractice()
            } else {
                startSelectionMode()
            }
        },
        modifier = Modifier.padding(16.dp)
            .onGloballyPositioned { extraListSpacerState.updateOverlay(it) },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {

        AnimatedContent(
            targetState = screenState.isSelectionModeEnabled.value,
            transitionSpec = {
                fadeIn(tween(150, 150)) togetherWith fadeOut(tween(150))
            }
        ) {
            if (it) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
            } else {
                Icon(
                    ExtraIcons.RadioButtonChecked,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun DeckDetailsConfigurationRow(
    configuration: DeckDetailsConfiguration.LetterDeckConfiguration,
    kanaGroupsMode: Boolean,
    onConfigurationUpdate: (DeckDetailsConfiguration.LetterDeckConfiguration) -> Unit,
) {

    var showFilterOptionDialog by remember { mutableStateOf(false) }
    if (showFilterOptionDialog) {
        DeckDetailsFilterDialog(
            filter = configuration.filterConfiguration,
            onDismissRequest = { showFilterOptionDialog = false },
            onApplyConfiguration = {
                showFilterOptionDialog = false
                onConfigurationUpdate(configuration.copy(filterConfiguration = it))
            }
        )
    }

    var showSortDialog by remember { mutableStateOf(false) }
    if (showSortDialog) {
        DeckDetailsSortDialog(
            sortOption = configuration.sortOption,
            isDesc = configuration.isDescending,
            onDismissRequest = { showSortDialog = false },
            onApplyClick = { sort, isDesc ->
                showSortDialog = false
                onConfigurationUpdate(configuration.copy(sortOption = sort, isDescending = isDesc))
            }
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        FilterChip(
            selected = true,
            onClick = {
                val newPracticeType = when (configuration.practiceType) {
                    ScreenLetterPracticeType.Writing -> ScreenLetterPracticeType.Reading
                    ScreenLetterPracticeType.Reading -> ScreenLetterPracticeType.Writing
                }
                onConfigurationUpdate(configuration.copy(practiceType = newPracticeType))
            },
            modifier = Modifier.wrapContentSize(Alignment.CenterStart),
            label = { Text(resolveString(configuration.practiceType.titleResolver)) },
            trailingIcon = {
                Icon(
                    imageVector = when (configuration.practiceType) {
                        ScreenLetterPracticeType.Writing -> Icons.Default.Draw
                        ScreenLetterPracticeType.Reading -> Icons.Default.LocalLibrary
                    },
                    contentDescription = null
                )
            }
        )
        if (kanaGroupsMode) {
            FilterChip(
                selected = true,
                enabled = false,
                onClick = {},
                modifier = Modifier.wrapContentSize(Alignment.CenterStart),
                label = { Text(resolveString { deckDetails.kanaGroupsModeActivatedLabel }) },
            )
        } else {

            SrsFilterChip(
                filterConfiguration = configuration.filterConfiguration,
                onClick = { showFilterOptionDialog = true }
            )

            FilterChip(
                selected = true,
                onClick = { showSortDialog = true },
                modifier = Modifier.wrapContentSize(Alignment.CenterStart),
                label = { Text(resolveString(configuration.sortOption.titleResolver)) },
                trailingIcon = {
                    Icon(
                        imageVector = configuration.sortOption.imageVector,
                        contentDescription = null,
                        modifier = Modifier.graphicsLayer {
                            rotationZ = if (configuration.isDescending) 90f else 270f
                        }
                    )
                }
            )
        }

    }
}

@Composable
private fun SrsFilterChip(
    filterConfiguration: FilterConfiguration,
    onClick: () -> Unit
) {
    FilterChip(
        selected = true,
        onClick = onClick,
        modifier = Modifier.wrapContentSize(Alignment.CenterStart),
        label = {
            Text(
                text = resolveString {
                    filterConfiguration.run {
                        when {
                            showNew && showDue && showDone -> deckDetails.filterAllLabel
                            !(showNew || showDue || showDone) -> deckDetails.filterNoneLabel
                            else -> {
                                val appliedFilters = mutableListOf<String>()
                                if (showNew) appliedFilters.add(reviewStateNew)
                                if (showDue) appliedFilters.add(reviewStateDue)
                                if (showDone) appliedFilters.add(reviewStateDone)
                                appliedFilters.joinToString()
                            }
                        }
                    }
                }
            )
        },
        trailingIcon = { Icon(Icons.Default.FilterAlt, null) }
    )
}

@Composable
fun DeckDetailsConfigurationRow(
    configuration: DeckDetailsConfiguration.VocabDeckConfiguration,
    onConfigurationUpdate: (DeckDetailsConfiguration.VocabDeckConfiguration) -> Unit,
) {

    var showFilterOptionDialog by remember { mutableStateOf(false) }
    if (showFilterOptionDialog) {
        DeckDetailsFilterDialog(
            filter = configuration.filterConfiguration,
            onDismissRequest = { showFilterOptionDialog = false },
            onApplyConfiguration = {
                showFilterOptionDialog = false
                onConfigurationUpdate(configuration.copy(filterConfiguration = it))
            }
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {

        FilterChip(
            selected = true,
            onClick = {
                val practiceTypes = ScreenVocabPracticeType.entries
                val newPracticeTypeOrdinal =
                    (configuration.practiceType.ordinal + 1) % practiceTypes.size
                val newPracticeType = practiceTypes[newPracticeTypeOrdinal]
                onConfigurationUpdate(configuration.copy(practiceType = newPracticeType))
            },
            modifier = Modifier.wrapContentSize(Alignment.CenterStart),
            label = { Text(resolveString(configuration.practiceType.titleResolver)) },
        )

        SrsFilterChip(
            filterConfiguration = configuration.filterConfiguration,
            onClick = { showFilterOptionDialog = true }
        )

    }

}


@Composable
fun DeckDetailsCharacterBox(
    character: String,
    srsStatus: SrsItemStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = character,
        fontSize = 32.textDp,
        modifier = modifier.size(60.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, srsStatus.toColor(), MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .wrapContentSize()
    )
}

