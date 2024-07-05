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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.CollapsibleContainer
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.rememberCollapsibleContainerState
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditItemActionIndicator
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditingMode
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditingModeSelector
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

    if (screenState.list.isEmpty()) {
        ScreenMessage(
            modifier = Modifier.fillMaxSize()
                .wrapContentSize()
                .widthIn(max = 400.dp)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        )
        return
    }

    val selectedMode = rememberSaveable { mutableStateOf(VocabDeckEditingMode.Details) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {

        val collapsibleContainerState = rememberCollapsibleContainerState()

        CollapsibleContainer(collapsibleContainerState) {
            Column {

                DeckEditingModeSelector(
                    selectedMode = selectedMode,
                    availableOptions = listOf(
                        VocabDeckEditingMode.Details,
                        VocabDeckEditingMode.Removal
                    )
                )

                AnimatedContent(
                    targetState = selectedMode.value,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    when (it) {
                        VocabDeckEditingMode.Details -> {
                            ScreenMessage(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        VocabDeckEditingMode.Removal,
                        VocabDeckEditingMode.ResetSrs -> Box(Modifier)
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(200.dp),
            modifier = Modifier.fillMaxWidth()
                .nestedScroll(collapsibleContainerState.nestedScrollConnection)
                .weight(1f)
                .onGloballyPositioned { extraListSpacerState.updateList(it) }
        ) {

            itemsIndexed(screenState.list) { index, listItem ->
                Row(
                    modifier = Modifier.fillMaxSize()
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            when (selectedMode.value) {
                                VocabDeckEditingMode.Details -> wordDialogData = listItem.word
                                VocabDeckEditingMode.Removal -> toggleRemoval(listItem)
                                VocabDeckEditingMode.ResetSrs -> TODO()
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

@Composable
private fun ScreenMessage(modifier: Modifier) {
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
        textAlign = TextAlign.Start,
        modifier = modifier
    )
}

private const val InlineIconId = "icon"
private const val InlineIconSizeValue = 24

private enum class VocabDeckEditingMode(
    override val icon: ImageVector,
    override val titleResolver: StringResolveScope<String>
) : DeckEditingMode {
    Details(
        icon = Icons.AutoMirrored.Filled.ViewList,
        titleResolver = { deckEdit.editingModeDetailsTitle }
    ),
    Removal(
        icon = Icons.Default.Close,
        titleResolver = { deckEdit.editingModeRemovalTitle }
    ),
    ResetSrs(
        icon = Icons.Default.Memory,
        titleResolver = { TODO() }
    )
}
