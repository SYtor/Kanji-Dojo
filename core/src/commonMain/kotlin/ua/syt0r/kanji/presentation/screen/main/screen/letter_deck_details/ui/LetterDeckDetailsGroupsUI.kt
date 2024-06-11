package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.RadioButtonChecked
import ua.syt0r.kanji.presentation.common.resources.icon.RadioButtonUnchecked
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.CustomRippleTheme
import ua.syt0r.kanji.presentation.common.ui.MultiplatformPopup
import ua.syt0r.kanji.presentation.common.ui.PreferredPopupLocation
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.LetterDeckDetailsCharacterBox
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.LetterDeckDetailsConfigurationRow
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.CharacterReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsListItem
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsVisibleData
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.PracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.toColor


private enum class GroupItemState { Default, Selected, Unselected }

@Composable
fun LetterDeckDetailsGroupsUI(
    visibleData: DeckDetailsVisibleData.Groups,
    extraListSpacerState: ExtraListSpacerState,
    onConfigurationUpdate: (LetterDeckDetailsConfiguration) -> Unit,
    selectGroup: (DeckDetailsListItem.Group) -> Unit,
    toggleGroupSelection: (DeckDetailsListItem.Group) -> Unit,
) {

    if (visibleData.items.isEmpty()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            LetterDeckDetailsConfigurationRow(
                configuration = visibleData.configuration,
                kanaGroupsMode = visibleData.kanaGroupsMode,
                onConfigurationUpdate = onConfigurationUpdate
            )
            Text(
                text = resolveString { practicePreview.emptyListMessage },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .wrapContentSize()
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .wrapContentSize(Alignment.TopCenter)
    ) {

        item(
            span = { GridItemSpan(maxLineSpan) }
        ) {
            LetterDeckDetailsConfigurationRow(
                configuration = visibleData.configuration,
                kanaGroupsMode = visibleData.kanaGroupsMode,
                onConfigurationUpdate = onConfigurationUpdate
            )
        }

        items(
            items = visibleData.items,
            key = { it.index }
        ) { group ->

            PracticeGroup(
                group = group,
                state = when {
                    !visibleData.isSelectionModeEnabled.value -> GroupItemState.Default
                    group.selected.value -> GroupItemState.Selected
                    else -> GroupItemState.Unselected
                },
                onClick = {
                    if (visibleData.isSelectionModeEnabled.value) {
                        toggleGroupSelection(group)
                    } else {
                        selectGroup(group)
                    }
                },
                modifier = Modifier
            )

        }

        extraListSpacerState.ExtraSpacer(this)

    }

}


@Composable
private fun PracticeGroup(
    group: DeckDetailsListItem.Group,
    state: GroupItemState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .background(group.reviewState.toColor(), CircleShape)
                .size(8.dp)
        )

        Text(
            text = resolveString {
                practicePreview.listGroupTitle(
                    group.index,
                    group.items.joinToString("") { it.character }
                )
            },
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                // TODO check when new font api is stable, currently LineHeightStyle.Alignment.Center
                //  with disabled font paddings doesn't help
                .padding(bottom = 1.dp),
            overflow = TextOverflow.Ellipsis,
        )

        if (state != GroupItemState.Default) {
            Icon(
                imageVector = if (state == GroupItemState.Selected) ExtraIcons.RadioButtonChecked
                else ExtraIcons.RadioButtonUnchecked,
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

    }

}
