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
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
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
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.formattedVocabReading
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.theme.errorColors
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
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

    var wordDialogData by remember { mutableStateOf<JapaneseWord?>(null) }
    wordDialogData?.let {
        AlternativeWordsDialog(
            word = it,
            onDismissRequest = { wordDialogData = null }
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

                val buttonIcon: ImageVector
                val colors: ListItemColors

                when (listItem.action.value) {
                    DeckEditItemAction.Nothing -> {
                        buttonIcon = Icons.Default.Delete
                        colors = ListItemDefaults.colors()
                    }

                    DeckEditItemAction.Add -> {
                        buttonIcon = Icons.Default.Delete
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.extraColorScheme.success,
                        )
                    }

                    DeckEditItemAction.Remove -> {
                        buttonIcon = Icons.AutoMirrored.Filled.Redo
                        colors = ListItemDefaults.errorColors()
                    }
                }

                val card = listItem.card.data

                ListItem(
                    leadingContent = {
                        DeckEditItemActionIndicator(listItem.action)
                    },
                    headlineContent = {
                        FuriganaText(
                            furiganaString = formattedVocabReading(
                                card.kanaReading,
                                card.kanjiReading
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    },
                    supportingContent = {
                        Text(listItem.card.resolvedCard.glossary.joinToString())
                    },
                    trailingContent = {
                        Row {
                            IconButton(
                                onClick = { }
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
//                    colors = colors,
                    modifier = Modifier.fillMaxSize()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { }
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
