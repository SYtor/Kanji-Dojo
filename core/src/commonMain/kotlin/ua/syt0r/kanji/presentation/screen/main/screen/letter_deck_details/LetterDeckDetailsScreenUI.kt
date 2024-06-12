package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.RadioButtonChecked
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.LetterDeckDetailsContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.CharacterReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsListItem
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsVisibleData
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.PracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui.DeckDetailsLayoutDialog
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui.LetterDeckDetailsBottomSheet
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui.LetterDeckDetailsGroupsUI
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui.LetterDeckDetailsItemsUI
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui.LetterDeckDetailsToolbar
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui.PracticePreviewScreenFilterOptionDialog
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui.PracticePreviewScreenSortDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterDeckDetailsScreenUI(
    state: State<ScreenState>,
    updateConfiguration: (LetterDeckDetailsConfiguration) -> Unit,
    navigateUp: () -> Unit,
    navigateToDeckEdit: () -> Unit,
    navigateToCharacterDetails: (String) -> Unit,
    showGroupDetails: (DeckDetailsListItem.Group) -> Unit,
    startSelectionMode: () -> Unit,
    leaveSelectionMode: () -> Unit,
    toggleSelection: (DeckDetailsListItem) -> Unit,
    selectAllClick: () -> Unit,
    deselectAllClick: () -> Unit,
    startGroupReview: (DeckDetailsListItem.Group) -> Unit,
    startMultiselectReview: () -> Unit,
) {

    var shouldShowVisibilityDialog by remember { mutableStateOf(false) }
    if (shouldShowVisibilityDialog) {
        val configuration = (state.value as ScreenState.Loaded).visibleDataState.value.configuration
        DeckDetailsLayoutDialog(
            layout = configuration.layout,
            kanaGroups = configuration.kanaGroups,
            onDismissRequest = { shouldShowVisibilityDialog = false },
            onApplyConfiguration = { layout, kanaGroups ->
                shouldShowVisibilityDialog = false
                updateConfiguration(configuration.copy(layout = layout, kanaGroups = kanaGroups))
            }
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val practiceSharer = rememberPracticeSharer(snackbarHostState)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    if (scaffoldState.bottomSheetState.isVisible) {
        MultiplatformBackHandler { coroutineScope.launch { scaffoldState.bottomSheetState.hide() } }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetTonalElevation = 0.dp,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            LetterDeckDetailsBottomSheet(
                state = state,
                onCharacterClick = navigateToCharacterDetails,
                onStudyClick = startGroupReview,
                onDismissRequest = { scaffoldState.bottomSheetState.hide() }
            )
        },
        topBar = {
            LetterDeckDetailsToolbar(
                state = state,
                upButtonClick = navigateUp,
                dismissMultiSelectButtonClick = leaveSelectionMode,
                onVisibilityButtonClick = { shouldShowVisibilityDialog = true },
                editButtonClick = navigateToDeckEdit,
                selectAllClick = selectAllClick,
                deselectAllClick = deselectAllClick,
                shareButtonClick = { practiceSharer.share(it) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        val extraListSpacerState = rememberExtraListSpacerState()

        val transition = updateTransition(targetState = state.value, label = "State Transition")
        transition.AnimatedContent(
            contentKey = { it::class },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .onGloballyPositioned { extraListSpacerState.updateList(it) }
        ) { screenState ->

            when (screenState) {
                ScreenState.Loading -> {
                    FancyLoading(Modifier.fillMaxSize().wrapContentSize())
                }

                is ScreenState.Loaded -> {
                    ScreenLoadedState(
                        screenState = screenState,
                        extraListSpacerState = extraListSpacerState,
                        onConfigurationUpdate = updateConfiguration,
                        onCharacterClick = navigateToCharacterDetails,
                        selectGroup = {
                            showGroupDetails(it)
                            coroutineScope.launch { scaffoldState.bottomSheetState.expand() }
                        },
                        toggleItemSelection = toggleSelection
                    )

                    if (screenState.visibleDataState.value.isSelectionModeEnabled.value) {
                        MultiplatformBackHandler(onBack = leaveSelectionMode)
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

            val noGroupsSelectedMessage = resolveString {
                letterDeckDetails.multiselectNoSelected
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
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = noGroupsSelectedMessage,
                                withDismissAction = true
                            )
                        }
                    }
                },
                startSelectionMode = startSelectionMode
            )
        }

    }

}

@Composable
private fun ScreenLoadedState(
    screenState: ScreenState.Loaded,
    extraListSpacerState: ExtraListSpacerState,
    onConfigurationUpdate: (LetterDeckDetailsConfiguration) -> Unit,
    onCharacterClick: (String) -> Unit,
    selectGroup: (DeckDetailsListItem.Group) -> Unit,
    toggleItemSelection: (DeckDetailsListItem) -> Unit,
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AnimatedContent(
            targetState = screenState.visibleDataState.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.fillMaxSize()
        ) {

            when (it) {
                is DeckDetailsVisibleData.Groups -> {
                    LetterDeckDetailsGroupsUI(
                        visibleData = it,
                        extraListSpacerState = extraListSpacerState,
                        onConfigurationUpdate = onConfigurationUpdate,
                        selectGroup = selectGroup,
                        toggleGroupSelection = toggleItemSelection
                    )
                }

                is DeckDetailsVisibleData.Items -> {
                    LetterDeckDetailsItemsUI(
                        visibleData = it,
                        extraListSpacerState = extraListSpacerState,
                        onConfigurationUpdate = onConfigurationUpdate,
                        onCharacterClick = onCharacterClick,
                        onSelectionToggled = toggleItemSelection
                    )
                }
            }

        }

    }

}

@Composable
fun CharacterReviewState.toColor(): Color = when (this) {
    CharacterReviewState.Done -> MaterialTheme.extraColorScheme.success
    CharacterReviewState.Due -> MaterialTheme.extraColorScheme.outdated
    else -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun FAB(
    screenState: ScreenState.Loaded,
    extraListSpacerState: ExtraListSpacerState,
    startPractice: () -> Unit,
    startSelectionMode: () -> Unit,
) {

    val selectionModeEnabled = remember {
        derivedStateOf {
            screenState.visibleDataState.value.isSelectionModeEnabled.value
        }
    }

    FloatingActionButton(
        onClick = {
            if (selectionModeEnabled.value) {
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
            targetState = selectionModeEnabled.value,
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
fun LetterDeckDetailsConfigurationRow(
    configuration: LetterDeckDetailsConfiguration,
    kanaGroupsMode: Boolean,
    onConfigurationUpdate: (LetterDeckDetailsConfiguration) -> Unit,
) {

    var showFilterOptionDialog by remember { mutableStateOf(false) }
    if (showFilterOptionDialog) {
        PracticePreviewScreenFilterOptionDialog(
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
        PracticePreviewScreenSortDialog(
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
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
    ) {
        FilterChip(
            selected = true,
            onClick = {
                val newPracticeType = when (configuration.practiceType) {
                    PracticeType.Writing -> PracticeType.Reading
                    PracticeType.Reading -> PracticeType.Writing
                }
                onConfigurationUpdate(configuration.copy(practiceType = newPracticeType))
            },
            modifier = Modifier.wrapContentSize(Alignment.CenterStart),
            label = { Text(resolveString(configuration.practiceType.titleResolver)) },
            trailingIcon = { Icon(configuration.practiceType.imageVector, null) }
        )
        if (kanaGroupsMode) {
            FilterChip(
                selected = true,
                enabled = false,
                onClick = {},
                modifier = Modifier.wrapContentSize(Alignment.CenterStart),
                label = { Text(resolveString { letterDeckDetails.kanaGroupsModeActivatedLabel }) },
            )
        } else {
            FilterChip(
                selected = true,
                onClick = { showFilterOptionDialog = true },
                modifier = Modifier.wrapContentSize(Alignment.CenterStart),
                label = {
                    Text(
                        text = resolveString {
                            configuration.filterConfiguration.run {
                                when {
                                    showNew && showDue && showDone -> letterDeckDetails.filterAllLabel
                                    !(showNew || showDue || showDone) -> letterDeckDetails.filterNoneLabel
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
fun LetterDeckDetailsCharacterBox(
    character: String,
    reviewState: CharacterReviewState,
    onClick: () -> Unit,
) {
    Text(
        text = character,
        fontSize = 32.textDp,
        modifier = Modifier
            .size(60.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, reviewState.toColor(), MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .wrapContentSize()
    )
}

