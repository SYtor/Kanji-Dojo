package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.formattedVocabReading
import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.presentation.common.CollapsibleContainer
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.rememberCollapsibleContainerState
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.RadioButtonChecked
import ua.syt0r.kanji.presentation.common.resources.icon.RadioButtonUnchecked
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
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

        LazyColumn(
            modifier = Modifier.fillMaxSize()
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

    val srsIndicatorColor = when (vocab.statusMap.getValue(practiceType)) {
        SrsItemStatus.New -> MaterialTheme.extraColorScheme.new
        SrsItemStatus.Done -> MaterialTheme.extraColorScheme.success
        SrsItemStatus.Review -> MaterialTheme.extraColorScheme.due
    }

    Row(
        modifier = modifier.height(IntrinsicSize.Max)
            .fillMaxWidth()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.width(6.dp)
                .fillMaxHeight()
                .background(srsIndicatorColor, MaterialTheme.shapes.small)
        )

        FuriganaText(
            furiganaString = vocab.word.data.run {
                formattedVocabReading(kanaReading, kanjiReading)
            },
            modifier = Modifier.weight(1f)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        if (selectionMode) {
            Icon(
                imageVector = if (vocab.selected.value) ExtraIcons.RadioButtonChecked
                else ExtraIcons.RadioButtonUnchecked,
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

    }

}
