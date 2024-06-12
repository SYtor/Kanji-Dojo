package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.resources.icon.DeselectAll
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.SelectAll
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.LetterDeckDetailsContract

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LetterDeckDetailsToolbar(
    state: State<LetterDeckDetailsContract.ScreenState>,
    upButtonClick: () -> Unit,
    dismissMultiSelectButtonClick: () -> Unit,
    shareButtonClick: (String) -> Unit,
    onVisibilityButtonClick: () -> Unit,
    editButtonClick: () -> Unit,
    selectAllClick: () -> Unit,
    deselectAllClick: () -> Unit,
) {
    TopAppBar(
        title = { ToolbarTitle(state) },
        navigationIcon = {
            val shouldShowMultiselectDismissButton by remember {
                derivedStateOf {
                    state.value
                        .let { it as? LetterDeckDetailsContract.ScreenState.Loaded }
                        ?.visibleDataState?.value
                        ?.isSelectionModeEnabled?.value == true
                }
            }
            if (shouldShowMultiselectDismissButton) {
                IconButton(
                    onClick = dismissMultiSelectButtonClick
                ) {
                    Icon(Icons.Default.Close, null)
                }
            } else {
                IconButton(
                    onClick = upButtonClick
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
            }
        },
        actions = {
            ToolbarActions(
                state = state,
                shareButtonClick = shareButtonClick,
                onVisibilityButtonClick = onVisibilityButtonClick,
                editButtonClick = editButtonClick,
                selectAllClick = selectAllClick,
                deselectAllClick = deselectAllClick
            )
        }
    )
}

private sealed interface ToolbarTitleData {
    object Loading : ToolbarTitleData
    data class Default(val title: String) : ToolbarTitleData
    data class Selection(val count: Int) : ToolbarTitleData
}

@Composable
private fun ToolbarTitle(state: State<LetterDeckDetailsContract.ScreenState>) {
    val toolbarTitleData: ToolbarTitleData by remember {
        derivedStateOf {
            state.value.let { it as? LetterDeckDetailsContract.ScreenState.Loaded }
                ?.let { loadedState ->
                    if (loadedState.visibleDataState.value.isSelectionModeEnabled.value) {
                        ToolbarTitleData.Selection(
                            count = loadedState.visibleDataState.value
                                .items.count { it.selected.value }
                        )
                    } else {
                        ToolbarTitleData.Default(loadedState.title)
                    }
                }
                ?: ToolbarTitleData.Loading
        }
    }

    when (val data = toolbarTitleData) {
        ToolbarTitleData.Loading -> {}
        is ToolbarTitleData.Default -> {
            Text(text = data.title)
        }

        is ToolbarTitleData.Selection -> {
            Text(text = resolveString { letterDeckDetails.multiselectTitle(data.count) })
        }
    }
}

private enum class DisplayingToolbarActions { Nothing, Default, Selection }

@Composable
private fun ToolbarActions(
    state: State<LetterDeckDetailsContract.ScreenState>,
    shareButtonClick: (String) -> Unit,
    onVisibilityButtonClick: () -> Unit,
    editButtonClick: () -> Unit,
    selectAllClick: () -> Unit,
    deselectAllClick: () -> Unit,
) {

    val selectableDataState: DisplayingToolbarActions by remember {
        derivedStateOf {
            when (
                state.value.let { it as? LetterDeckDetailsContract.ScreenState.Loaded }
                    ?.visibleDataState?.value
                    ?.isSelectionModeEnabled?.value
            ) {
                true -> DisplayingToolbarActions.Selection
                false -> DisplayingToolbarActions.Default
                null -> DisplayingToolbarActions.Nothing
            }
        }
    }

    AnimatedContent(
        targetState = selectableDataState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
    ) {
        Row {
            when (it) {
                DisplayingToolbarActions.Nothing -> {
                    Box(Modifier.width(200.dp))
                }

                DisplayingToolbarActions.Default -> {
                    IconButton(
                        onClick = {
                            shareButtonClick((state.value as LetterDeckDetailsContract.ScreenState.Loaded).sharePractice)
                        }
                    ) {
                        Icon(Icons.Default.Share, null)
                    }
                    IconButton(
                        onClick = onVisibilityButtonClick
                    ) {
                        Icon(Icons.Default.Visibility, null)
                    }
                    IconButton(
                        onClick = editButtonClick
                    ) {
                        Icon(Icons.Default.Edit, null)
                    }
                }

                DisplayingToolbarActions.Selection -> {
                    IconButton(
                        onClick = deselectAllClick
                    ) {
                        Icon(ExtraIcons.DeselectAll, null)
                    }
                    IconButton(
                        onClick = selectAllClick
                    ) {
                        Icon(ExtraIcons.SelectAll, null)
                    }
                }
            }
        }
    }
}