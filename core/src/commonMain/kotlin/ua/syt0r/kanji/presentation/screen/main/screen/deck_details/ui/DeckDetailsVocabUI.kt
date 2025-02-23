package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.formattedVocabStringReading
import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.presentation.common.CollapsibleContainer
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.clickable
import ua.syt0r.kanji.presentation.common.copyCentered
import ua.syt0r.kanji.presentation.common.rememberCollapsibleContainerState
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.RadioButtonChecked
import ua.syt0r.kanji.presentation.common.resources.icon.RadioButtonUnchecked
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.DeckDetailsConfigurationRow
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.DeckDetailsScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsListItem
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsVisibleData

@Composable
fun DeckDetailsVocabUI(
    screenState: ScreenState.Loaded.Vocab,
    visibleData: DeckDetailsVisibleData.Vocab,
    extraListSpacerState: ExtraListSpacerState,
    onConfigurationUpdate: (DeckDetailsConfiguration.VocabDeckConfiguration) -> Unit,
    toggleItemSelection: (DeckDetailsListItem.Vocab) -> Unit,
    onCardClick: (DeckDetailsListItem.Vocab) -> Unit
) {

    if (visibleData.items.isEmpty()) {
        Column {
            DeckDetailsConfigurationRow(
                configuration = screenState.configuration.value,
                onConfigurationUpdate = onConfigurationUpdate
            )

            Text(
                text = resolveString { deckDetails.emptyListMessage },
                modifier = Modifier.padding(horizontal = 20.dp)
                    .weight(1f)
                    .fillMaxWidth()
                    .wrapContentSize()
            )
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        val collapsibleConfigurationContainerState = rememberCollapsibleContainerState()

        CollapsibleContainer(
            state = collapsibleConfigurationContainerState,
            modifier = Modifier.fillMaxWidth()
        ) {
            DeckDetailsConfigurationRow(
                configuration = screenState.configuration.value,
                onConfigurationUpdate = onConfigurationUpdate
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(360.dp),
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 20.dp)
                .nestedScroll(collapsibleConfigurationContainerState.nestedScrollConnection)
        ) {

            val selectionMode = screenState.isSelectionModeEnabled.value

            itemsIndexed(
                items = visibleData.items,
                key = { _, it -> it.key.value }
            ) { index, vocab ->

                WordItem(
                    listIndex = index,
                    vocab = vocab,
                    practiceType = screenState.configuration.value.practiceType,
                    selectionMode = selectionMode,
                    onClick = {
                        if (selectionMode) {
                            toggleItemSelection(vocab)
                        } else {
                            onCardClick(vocab)
                        }
                    },
                    modifier = Modifier
                )

            }

            extraListSpacerState.ExtraSpacer(this)

        }

    }
}


@Composable
private fun WordItem(
    listIndex: Int,
    vocab: DeckDetailsListItem.Vocab,
    practiceType: ScreenVocabPracticeType,
    selectionMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    ListItem(
        leadingContent = {
            val srsIndicatorColor = when (vocab.statusMap.getValue(practiceType)) {
                SrsItemStatus.New -> MaterialTheme.extraColorScheme.new
                SrsItemStatus.Done -> MaterialTheme.extraColorScheme.success
                SrsItemStatus.Review -> MaterialTheme.extraColorScheme.due
            }
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .widthIn(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxHeight()
                        .width(4.dp)
                        .background(srsIndicatorColor, MaterialTheme.shapes.small)
                )
                Text(
                    text = (listIndex + 1).toString(),
                    style = MaterialTheme.typography.bodyMedium.copyCentered(),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        },
        headlineContent = {
            val text = vocab.word.data
                .run { formattedVocabStringReading(kanaReading, kanjiReading) }
            Text(text)
        },
        supportingContent = {
            Text(vocab.word.data.meaning ?: "Dic meaning") // TODO
        },
        trailingContent = {
            if (selectionMode) {
                Icon(
                    imageVector = if (vocab.selected.value) ExtraIcons.RadioButtonChecked
                    else ExtraIcons.RadioButtonUnchecked,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        },
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick)
    )

}
