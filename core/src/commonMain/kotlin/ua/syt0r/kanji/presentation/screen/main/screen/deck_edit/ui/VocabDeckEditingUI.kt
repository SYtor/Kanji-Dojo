package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.formattedVocabStringReading
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditItemAction
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditItemActionIndicator
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.VocabDeckEditListItem

@Composable
fun VocabDeckEditingUI(
    screenState: ScreenState.VocabDeckEditing,
    extraListSpacerState: ExtraListSpacerState,
    toggleRemoval: (VocabDeckEditListItem) -> Unit
) {

    var editListItem by remember { mutableStateOf<VocabDeckEditListItem?>(null) }
    editListItem?.let {
        VocabEditDialog(
            state = rememberVocabEditDialogState(it),
            onDismissRequest = { editListItem = null }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth()
            .wrapContentWidth()
            .width(400.dp)
            .padding(horizontal = 20.dp)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onGloballyPositioned { extraListSpacerState.updateList(it) }
        ) {

            if (screenState.list.isEmpty()) {
                item {
                    Text(
                        text = "No words in the deck",
                        modifier = Modifier.fillMaxWidth().wrapContentWidth()
                    )
                }
            }

            itemsIndexed(screenState.list) { index, listItem ->

                val title: String
                val subtitle: String
                val buttonIcon: ImageVector

                when (val modifiedData = listItem.card.modifiedData.value) {
                    null -> {
                        val card = listItem.card.data
                        title = formattedVocabStringReading(card.kanaReading, card.kanjiReading)
                        subtitle = listItem.card.meaning
                    }

                    else -> {
                        title = formattedVocabStringReading(
                            modifiedData.kanaReading,
                            modifiedData.kanjiReading
                        )
                        subtitle = listItem.card.meaning
                    }
                }

                when (listItem.action.value) {
                    DeckEditItemAction.Nothing -> {
                        buttonIcon = Icons.Default.Delete
                    }

                    DeckEditItemAction.Add -> {
                        buttonIcon = Icons.Default.Delete
                    }

                    DeckEditItemAction.Remove -> {
                        buttonIcon = Icons.AutoMirrored.Filled.Redo
                    }
                }

                ListItem(
                    leadingContent = { DeckEditItemActionIndicator(listItem.action) },
                    headlineContent = { Text(title) },
                    supportingContent = { Text(subtitle) },
                    trailingContent = {
                        Row {
                            IconButton(
                                onClick = { editListItem = listItem }
                            ) {
                                Icon(Icons.Default.Edit, null)
                            }
                            IconButton(
                                onClick = { toggleRemoval(listItem) }
                            ) {
                                Icon(buttonIcon, null)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { TODO() }
                )

            }

            extraListSpacerState.ExtraSpacer(this)

        }

    }

}

@Composable
private fun ScreenMessage() {
    Text(
        text = resolveString { deckEdit.vocabDetailsMessage(InlineIconId) },
        inlineContent = mapOf(
            InlineIconId to InlineTextContent(
                Placeholder(
                    InlineIconSizeValue.textDp,
                    InlineIconSizeValue.textDp,
                    PlaceholderVerticalAlign.TextCenter
                ),
                children = {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        ),
        style = MaterialTheme.typography.bodySmall
    )
}

private const val InlineIconId = "icon"
private const val InlineIconSizeValue = 18
