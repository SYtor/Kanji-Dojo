package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditItemActionIndicator
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditingMode
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditingModeSelector
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.VocabDeckEditListItem

private const val InlineIconId = "icon"

@Composable
fun VocabDeckEditingUI(
    screenState: ScreenState.VocabDeckEditing,
    extraListSpacerState: ExtraListSpacerState,
    toggleRemoval: (VocabDeckEditListItem) -> Unit
) {

    val selectedMode = rememberSaveable { mutableStateOf(DeckEditingMode.Search) }

    var wordDialogData by remember { mutableStateOf<JapaneseWord?>(null) }
    wordDialogData?.let {
        AlternativeWordsDialog(
            word = it,
            onDismissRequest = { wordDialogData = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        DeckEditingModeSelector(
            selectedMode = selectedMode,
            availableOptions = listOf(DeckEditingMode.Search, DeckEditingMode.Removal)
        )

        AnimatedContent(
            targetState = selectedMode.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.fillMaxWidth()
        ) {
            when (it) {
                DeckEditingMode.Search -> {
                    Text(
                        text = resolveString { deckEdit.vocabSearchMessage(InlineIconId) },
                        inlineContent = mapOf(
                            InlineIconId to InlineTextContent(
                                Placeholder(
                                    24.textDp,
                                    24.textDp,
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
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                            .wrapContentWidth()
                            .widthIn(max = 400.dp)
                    )
                }

                DeckEditingMode.Removal,
                DeckEditingMode.ResetSrs -> Box(Modifier)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(200.dp),
            modifier = Modifier.fillMaxWidth()
                .padding(top = 20.dp)
                .weight(1f)
                .onGloballyPositioned { extraListSpacerState.updateList(it) }
        ) {

            itemsIndexed(screenState.list) { index, listItem ->
                Row(
                    modifier = Modifier.fillMaxSize()
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            when (selectedMode.value) {
                                DeckEditingMode.Search -> wordDialogData = listItem.word
                                DeckEditingMode.Removal -> toggleRemoval(listItem)
                                DeckEditingMode.ResetSrs -> TODO()
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FuriganaText(
                        furiganaString = listItem.word.orderedPreview(index),
                        modifier = Modifier.weight(1f).alignByBaseline()
                    )
                    DeckEditItemActionIndicator(
                        action = listItem.action,
                        Modifier.alignBy(FirstBaseline)
                    )
                }
            }

            extraListSpacerState.ExtraSpacer(this)

        }

    }

}