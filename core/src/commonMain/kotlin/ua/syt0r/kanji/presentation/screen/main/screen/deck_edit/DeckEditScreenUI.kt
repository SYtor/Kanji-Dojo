package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.Save
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.ui.DeleteWritingPracticeDialog
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.ui.LetterDeckEditingUI
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.ui.PracticeCreateLeaveConfirmation
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.ui.PracticeCreateUnknownCharactersDialog
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.ui.SaveWritingPracticeDialog
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.ui.VocabDeckEditingUI

@Composable
fun DeckEditScreenUI(
    configuration: DeckEditScreenConfiguration,
    state: State<ScreenState>,
    navigateBack: () -> Unit,
    submitSearch: (String) -> Unit,
    toggleRemoval: (DeckEditListItem) -> Unit,
    onCharacterInfoClick: (String) -> Unit,
    saveChanges: () -> Unit,
    deleteDeck: () -> Unit,
    onCompleted: () -> Unit
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val extraListSpacerState = rememberExtraListSpacerState()

    var showLeaveConfirmationDialog by remember { mutableStateOf(false) }
    if (showLeaveConfirmationDialog) {
        PracticeCreateLeaveConfirmation(
            onDismissRequest = { showLeaveConfirmationDialog = false },
            onConfirmation = navigateBack
        )
    }

    val shouldHandleBackClick by remember {
        derivedStateOf {
            state.value.let { it as? ScreenState.Loaded }?.confirmExit?.value ?: false
        }
    }

    if (shouldHandleBackClick) {
        MultiplatformBackHandler { showLeaveConfirmationDialog = true }
    }

    Scaffold(
        topBar = {
            Toolbar(
                configuration = configuration,
                navigateUp = {
                    if (shouldHandleBackClick) {
                        showLeaveConfirmationDialog = true
                    } else {
                        navigateBack()
                    }
                },
                onDeleteConfirmed = deleteDeck
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        val transition = updateTransition(
            targetState = state.value,
            label = "State Update Transition"
        )
        transition.AnimatedContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it::class }
        ) { screenState ->

            when (screenState) {
                ScreenState.Loading -> FancyLoading(Modifier.fillMaxSize().wrapContentSize())

                is ScreenState.Loaded -> {
                    LoadedState(
                        screenState = screenState,
                        extraListSpacerState = extraListSpacerState,
                        onInputSubmit = submitSearch,
                        onInfoClick = onCharacterInfoClick,
                        toggleRemoval = toggleRemoval,
                        onSaveConfirmed = saveChanges
                    )
                }

                ScreenState.Deleting -> Text("Del")
                ScreenState.SavingChanges -> Text("Sav")
                ScreenState.Completed -> {
                    Text("Don")
                    LaunchedEffect(Unit) {
                        delay(600)
                        onCompleted()
                    }
                }

            }

        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Toolbar(
    configuration: DeckEditScreenConfiguration,
    navigateUp: () -> Unit,
    onDeleteConfirmed: () -> Unit
) {

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    if (showDeleteConfirmationDialog) {
        DeleteWritingPracticeDialog(
            configuration = configuration as DeckEditScreenConfiguration.EditExisting,
            onDismissRequest = { showDeleteConfirmationDialog = false },
            onDeleteConfirmed = onDeleteConfirmed
        )
    }

    TopAppBar(
        title = {
            Text(
                text = resolveString {
                    when (configuration) {
                        is DeckEditScreenConfiguration.LetterDeck.Edit,
                        is DeckEditScreenConfiguration.VocabDeck.Edit -> practiceCreate.ediTitle

                        else -> practiceCreate.newTitle
                    }
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }
        },
        actions = {
            if (configuration is DeckEditScreenConfiguration.EditExisting) {
                IconButton(
                    onClick = { showDeleteConfirmationDialog = true }
                ) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
    )
}

@Composable
private fun LoadedState(
    screenState: ScreenState.Loaded,
    extraListSpacerState: ExtraListSpacerState,
    onInputSubmit: (String) -> Unit,
    onInfoClick: (String) -> Unit,
    toggleRemoval: (DeckEditListItem) -> Unit,
    onSaveConfirmed: () -> Unit
) {

    var showTitleInputDialog by remember { mutableStateOf(false) }
    if (showTitleInputDialog) {
        SaveWritingPracticeDialog(
            title = screenState.title,
            onConfirm = onSaveConfirmed,
            onDismissRequest = { showTitleInputDialog = false }
        )
    }

    var unknownEnteredCharacters: List<String> by remember { mutableStateOf(emptyList()) }
    if (unknownEnteredCharacters.isNotEmpty()) {
        PracticeCreateUnknownCharactersDialog(
            characters = unknownEnteredCharacters,
            onDismissRequest = { unknownEnteredCharacters = emptyList() }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when (screenState) {
            is ScreenState.LetterDeckEditing -> {
                LetterDeckEditingUI(
                    screenState = screenState,
                    extraListSpacerState = extraListSpacerState,
                    submitSearch = onInputSubmit,
                    showCharacterInfo = onInfoClick,
                    toggleRemoval = toggleRemoval
                )
            }

            is ScreenState.VocabDeckEditing -> {
                VocabDeckEditingUI(screenState)
            }
        }

        val shouldShow = remember {
            derivedStateOf {
                val letterEditingState = screenState as? ScreenState.LetterDeckEditing
                letterEditingState?.searching?.value?.not() ?: true
            }
        }

        AnimatedVisibility(
            visible = shouldShow.value,
            enter = scaleIn(),
            exit = scaleOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            FloatingActionButton(
                modifier = Modifier.padding(20.dp)
                    .onGloballyPositioned { extraListSpacerState.updateOverlay(it) },
                onClick = { showTitleInputDialog = true },
                content = { Icon(ExtraIcons.Save, null) }
            )
        }

    }

}

@Composable
fun DeckEditingModeSelector(
    selectedMode: MutableState<DeckEditingMode>,
    availableOptions: List<DeckEditingMode> = DeckEditingMode.values().toList()
) {

    Row(
        modifier = Modifier.fillMaxWidth()
            .wrapContentWidth()
            .height(IntrinsicSize.Min)
            .wrapContentWidth()
            .width(IntrinsicSize.Min)
            .clip(MaterialTheme.shapes.large)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableOptions.forEach {
            Row(
                modifier = Modifier.weight(1f)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        color = when {
                            selectedMode.value != it -> MaterialTheme.colorScheme.surface
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    .clickable { selectedMode.value = it }
                    .padding(12.dp)
                    .wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(it.icon, null)
                Text(text = it.name)
            }
        }
    }

}