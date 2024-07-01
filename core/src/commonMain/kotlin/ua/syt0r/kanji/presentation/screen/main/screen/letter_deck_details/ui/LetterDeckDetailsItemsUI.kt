package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.CollapsibleContainer
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.rememberCollapsibleContainerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.LetterDeckDetailsCharacterBox
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.LetterDeckDetailsConfigurationRow
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsListItem
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsVisibleData
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.PracticeType

@Composable
fun LetterDeckDetailsItemsUI(
    visibleData: DeckDetailsVisibleData.Items,
    extraListSpacerState: ExtraListSpacerState,
    onConfigurationUpdate: (LetterDeckDetailsConfiguration) -> Unit,
    onCharacterClick: (String) -> Unit,
    onSelectionToggled: (DeckDetailsListItem) -> Unit,
) {

    Column {

        val collapsibleConfigurationContainerState = rememberCollapsibleContainerState()

        CollapsibleContainer(collapsibleConfigurationContainerState) {
            LetterDeckDetailsConfigurationRow(
                configuration = visibleData.configuration,
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
                key = { it.item.character },
            ) {
                LetterListItem(
                    it = it,
                    visibleData = visibleData,
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
    it: DeckDetailsListItem.Letter,
    visibleData: DeckDetailsVisibleData.Items,
    onCharacterClick: (String) -> Unit,
    onSelectionToggled: (DeckDetailsListItem.Letter) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(visibleData.isSelectionModeEnabled.value) {
                onSelectionToggled(it)
            }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {

        val summary = when (visibleData.configuration.practiceType) {
            PracticeType.Writing -> it.item.writingSummary
            PracticeType.Reading -> it.item.readingSummary
        }

        LetterDeckDetailsCharacterBox(
            character = it.item.character,
            reviewState = summary.state,
            onClick = { onCharacterClick(it.item.character) }
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            val strings = resolveString { letterDeckDetails }

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

        if (visibleData.isSelectionModeEnabled.value) {
            RadioButton(
                selected = it.selected.value,
                onClick = { onSelectionToggled(it) },
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

    }

}
