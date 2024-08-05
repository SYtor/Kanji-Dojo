package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui

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
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.DeckDetailsScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsListItem

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DeckDetailsToolbar(
    state: State<ScreenState>,
    upButtonClick: () -> Unit,
    shareButtonClick: (String) -> Unit,
    onVisibilityButtonClick: () -> Unit,
    editButtonClick: () -> Unit,
) {

    TopAppBar(
        title = { ToolbarTitle(state) },
        navigationIcon = {
            NavigationIcon(
                state = state,
                upButtonClick = upButtonClick
            )
        },
        actions = {
            ToolbarActions(
                state = state,
                shareButtonClick = shareButtonClick,
                onVisibilityButtonClick = onVisibilityButtonClick,
                editButtonClick = editButtonClick
            )
        }
    )

}

@Composable
fun NavigationIcon(
    state: State<ScreenState>,
    upButtonClick: () -> Unit
) {
    val loadedState = remember {
        derivedStateOf { state.value.let { it as? ScreenState.Loaded } }
    }.value

    when {
        loadedState != null && loadedState.isSelectionModeEnabled.value -> {
            IconButton(
                onClick = { loadedState.isSelectionModeEnabled.value = false }
            ) {
                Icon(Icons.Default.Close, null)
            }
        }

        else -> {
            IconButton(
                onClick = upButtonClick
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        }
    }
}

private sealed interface ToolbarTitleData {
    object Loading : ToolbarTitleData
    data class Default(val title: String) : ToolbarTitleData
    data class Selection(val count: Int) : ToolbarTitleData
}

@Composable
private fun ToolbarTitle(state: State<ScreenState>) {
    val toolbarTitleData: ToolbarTitleData by remember {
        derivedStateOf {
            state.value.let { it as? ScreenState.Loaded }
                ?.let { loadedState ->
                    if (loadedState.isSelectionModeEnabled.value) {
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
            Text(text = resolveString { deckDetails.multiselectTitle(data.count) })
        }
    }
}

private sealed interface DisplayingToolbarActions {
    data class Default(val sharableDeckData: String?) : DisplayingToolbarActions
    object Nothing : DisplayingToolbarActions
    data class Selection(val items: List<DeckDetailsListItem>) : DisplayingToolbarActions
}

@Composable
private fun ToolbarActions(
    state: State<ScreenState>,
    shareButtonClick: (String) -> Unit,
    onVisibilityButtonClick: () -> Unit,
    editButtonClick: () -> Unit,
) {

    val selectableDataState: DisplayingToolbarActions by remember {
        derivedStateOf {
            val loadedState = state.value.let { it as? ScreenState.Loaded }
            when (
                loadedState?.isSelectionModeEnabled?.value
            ) {
                true -> DisplayingToolbarActions.Selection(
                    items = loadedState.visibleDataState.value.items
                )

                false -> DisplayingToolbarActions.Default(
                    sharableDeckData = loadedState.let { it as? ScreenState.Loaded.Letters }
                        ?.sharableDeckData
                )

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

                is DisplayingToolbarActions.Default -> {
                    if (it.sharableDeckData != null) {
                        IconButton(
                            onClick = { shareButtonClick(it.sharableDeckData) }
                        ) {
                            Icon(Icons.Default.Share, null)
                        }
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

                is DisplayingToolbarActions.Selection -> {
                    IconButton(
                        onClick = { it.items.forEach { it.selected.value = false } }
                    ) {
                        Icon(ExtraIcons.DeselectAll, null)
                    }
                    IconButton(
                        onClick = { it.items.forEach { it.selected.value = true } }
                    ) {
                        Icon(ExtraIcons.SelectAll, null)
                    }
                }
            }
        }
    }
}