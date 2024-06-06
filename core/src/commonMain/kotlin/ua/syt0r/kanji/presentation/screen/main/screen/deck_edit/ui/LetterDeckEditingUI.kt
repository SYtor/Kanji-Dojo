package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditItemActionIndicator
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditListItem
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditingMode
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditingModeSelector
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.LetterDeckEditListItem


@Composable
fun LetterDeckEditingUI(
    screenState: ScreenState.LetterDeckEditing,
    extraListSpacerState: ExtraListSpacerState,
    submitSearch: (String) -> Unit,
    showCharacterInfo: (String) -> Unit,
    toggleRemoval: (DeckEditListItem) -> Unit,
) {

    val deckEditingMode = rememberSaveable { mutableStateOf(DeckEditingMode.Search) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        DeckEditingModeSelector(deckEditingMode)

        when (deckEditingMode.value) {
            DeckEditingMode.Search -> {
                CharacterInputField(
                    isEnabled = !screenState.searching.value,
                    onInputSubmit = submitSearch
                )
            }

            DeckEditingMode.Removal -> {

            }

            DeckEditingMode.ResetSrs -> {

            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(50.dp),
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
                .onGloballyPositioned { extraListSpacerState.updateList(it) }
        ) {

            items(screenState.listState.value) {
                ListItem(
                    item = it,
                    onClick = {
                        when (deckEditingMode.value) {
                            DeckEditingMode.Search -> showCharacterInfo(it.character)
                            DeckEditingMode.Removal -> toggleRemoval(it)
                            DeckEditingMode.ResetSrs -> TODO()
                        }
                    }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                extraListSpacerState.ExtraSpacer()
            }

        }

    }

}

@Composable
private fun CharacterInputField(
    isEnabled: Boolean,
    onInputSubmit: (String) -> Unit
) {

    var enteredText by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }

    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val handleSubmit: () -> Unit = {
        onInputSubmit(enteredText)
        enteredText = ""
        softwareKeyboardController?.hide()
    }

    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = { enteredText = "" }
        ) {
            Icon(Icons.Default.Close, null)
        }

        Box(modifier = Modifier.weight(1f)) {

            var isInputFocused by remember { mutableStateOf(false) }

            BasicTextField(
                value = enteredText,
                onValueChange = { enteredText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isInputFocused = it.isFocused },
                maxLines = 1,
                singleLine = true,
                interactionSource = interactionSource,
                cursorBrush = SolidColor(color),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions { handleSubmit() }
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = !isInputFocused && enteredText.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = resolveString { practiceCreate.searchHint },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

        }

        IconButton(
            onClick = handleSubmit,
            enabled = isEnabled
        ) {
            Icon(Icons.Default.Search, null)
        }

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyGridItemScope.ListItem(
    item: LetterDeckEditListItem,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier.animateItemPlacement()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {

        Text(
            text = item.character,
            modifier = Modifier.align(Alignment.Center),
            fontSize = 32.sp
        )

        DeckEditItemActionIndicator(
            action = item.action,
            modifier = Modifier.align(Alignment.BottomEnd)
        )

    }

}
