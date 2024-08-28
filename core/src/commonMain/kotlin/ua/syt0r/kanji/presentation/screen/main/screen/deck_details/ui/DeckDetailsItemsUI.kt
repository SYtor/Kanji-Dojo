package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.CollapsibleContainer
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.rememberCollapsibleContainerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.DeckDetailsCharacterBox
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.DeckDetailsConfigurationRow
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsListItem
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsVisibleData


@Composable
fun DeckDetailsItemsUI(
    configuration: DeckDetailsConfiguration.LetterDeckConfiguration,
    selectionModeEnabled: MutableState<Boolean>,
    visibleData: DeckDetailsVisibleData.Items,
    extraListSpacerState: ExtraListSpacerState,
    onConfigurationUpdate: (DeckDetailsConfiguration.LetterDeckConfiguration) -> Unit,
    onCharacterClick: (String) -> Unit,
    onSelectionToggled: (DeckDetailsListItem) -> Unit,
) {

    if (visibleData.items.isEmpty()) {
        Column {
            DeckDetailsConfigurationRow(
                configuration = configuration,
                kanaGroupsMode = false,
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

    Column {

        val collapsibleConfigurationContainerState = rememberCollapsibleContainerState()

        CollapsibleContainer(collapsibleConfigurationContainerState) {
            DeckDetailsConfigurationRow(
                configuration = configuration,
                kanaGroupsMode = false,
                onConfigurationUpdate = onConfigurationUpdate
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(300.dp),
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = 20.dp)
                .nestedScroll(collapsibleConfigurationContainerState.nestedScrollConnection)
        ) {

            items(
                items = visibleData.items,
                key = { it.key.value },
            ) {
                LetterListItem(
                    item = it,
                    isSelectionModeEnabled = selectionModeEnabled,
                    configuration = configuration,
                    onCharacterClick = onCharacterClick,
                    onSelectionToggled = onSelectionToggled
                )
            }

            extraListSpacerState.ExtraSpacer(this)

        }

    }

}

@Composable
private fun LetterListItem(
    item: DeckDetailsListItem.Letter,
    isSelectionModeEnabled: State<Boolean>,
    configuration: DeckDetailsConfiguration.LetterDeckConfiguration,
    onCharacterClick: (String) -> Unit,
    onSelectionToggled: (DeckDetailsListItem.Letter) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(isSelectionModeEnabled.value) {
                onSelectionToggled(item)
            }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {

        val summary = when (configuration.practiceType) {
            ScreenLetterPracticeType.Writing -> item.data.writingSummary
            ScreenLetterPracticeType.Reading -> item.data.readingSummary
        }

        DeckDetailsCharacterBox(
            character = item.data.character,
            reviewState = summary.srsItemStatus,
            onClick = { onCharacterClick(item.data.character) }
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            val strings = resolveString { deckDetails }

            Text(
                text = strings.expectedReviewDate(summary.expectedReviewDate),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = strings.lastReviewDate(summary.lastReviewDate),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = strings.repetitions(summary.repeats),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = strings.lapses(summary.lapses),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (isSelectionModeEnabled.value) {
            RadioButton(
                selected = item.selected.value,
                onClick = { onSelectionToggled(item) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

    }

}
