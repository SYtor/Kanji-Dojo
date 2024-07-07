package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.Help
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.customBlue
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.MultiplatformPopup
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsLayout
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.FilterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.SortOption


private data class FilterCheckboxRowData(
    val valueState: MutableState<Boolean>,
    val titleResolver: StringResolveScope<String>,
    val dotColor: Color
)

@Composable
fun LetterDeckDetailsFilterDialog(
    filter: FilterConfiguration,
    onDismissRequest: () -> Unit,
    onApplyConfiguration: (FilterConfiguration) -> Unit
) {

    val new = remember { mutableStateOf(filter.showNew) }
    val due = remember { mutableStateOf(filter.showDue) }
    val done = remember { mutableStateOf(filter.showDone) }

    val filterRowsData = listOf(
        FilterCheckboxRowData(
            valueState = new,
            titleResolver = { reviewStateNew },
            dotColor = customBlue
        ),
        FilterCheckboxRowData(
            valueState = due,
            titleResolver = { reviewStateDue },
            dotColor = MaterialTheme.extraColorScheme.outdated
        ),
        FilterCheckboxRowData(
            valueState = done,
            titleResolver = { reviewStateDone },
            dotColor = MaterialTheme.extraColorScheme.success
        )
    )

    BaseDialog(
        title = resolveString { letterDeckDetails.filterDialog.title },
        onDismissRequest = onDismissRequest,
        onApplyClick = {
            onApplyConfiguration(
                FilterConfiguration(
                    showNew = new.value,
                    showDue = due.value,
                    showDone = done.value
                )
            )
        }
    ) {
        filterRowsData.forEach { FilterRow(it) }
    }

}

@Composable
private fun FilterRow(data: FilterCheckboxRowData) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = { data.valueState.value = !data.valueState.value })
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Box(
            modifier = Modifier
                .alignBy { it.measuredHeight }
                .size(10.dp)
                .clip(CircleShape)
                .background(data.dotColor)
        )

        Text(
            text = resolveString(data.titleResolver),
            modifier = Modifier.weight(1f).alignByBaseline()
        )

        val extraIconPadding = with(LocalDensity.current) { 5.dp.roundToPx() }

        Icon(
            imageVector = when (data.valueState.value) {
                true -> Icons.Default.CheckCircle
                false -> Icons.Default.RemoveCircle
            },
            contentDescription = null,
            tint = when (data.valueState.value) {
                true -> MaterialTheme.extraColorScheme.success
                false -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier
                .size(24.dp)
                .alignBy { it.measuredHeight - extraIconPadding }
        )

    }
}

@Composable
fun LetterDeckDetailsSortDialog(
    onDismissRequest: () -> Unit,
    onApplyClick: (sortOption: SortOption, isDescending: Boolean) -> Unit,
    sortOption: SortOption,
    isDesc: Boolean
) {

    var selectedSortOption by rememberSaveable { mutableStateOf(sortOption) }
    var isDescending by rememberSaveable { mutableStateOf(isDesc) }

    BaseDialog(
        title = resolveString { letterDeckDetails.sortDialog.title },
        onDismissRequest = onDismissRequest,
        onApplyClick = { onApplyClick(selectedSortOption, isDescending) }
    ) {

        SortOption.values().forEach {

            SelectableRow(
                isSelected = it == selectedSortOption,
                onClick = {
                    if (selectedSortOption == it) isDescending = !isDescending
                    else selectedSortOption = it
                }
            ) {

                Text(
                    text = resolveString(it.titleResolver),
                    modifier = Modifier.padding(start = 14.dp)
                )

                var showHint by remember { mutableStateOf(false) }

                IconButton(onClick = { showHint = true }) {
                    Icon(ExtraIcons.Help, null)
                }

                MultiplatformPopup(
                    expanded = showHint,
                    onDismissRequest = { showHint = false }
                ) {
                    Text(
                        text = resolveString(it.hintResolver),
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                            .widthIn(max = 200.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (it == selectedSortOption) {
                    val rotation by animateFloatAsState(
                        targetValue = if (isDescending) 90f else 270f
                    )
                    IconButton(onClick = { isDescending = !isDescending }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.graphicsLayer(rotationZ = rotation)
                        )
                    }
                }

            }

        }
    }

}


@Composable
fun LetterDeckDetailsLayoutDialog(
    layout: DeckDetailsLayout,
    kanaGroups: Boolean,
    onDismissRequest: () -> Unit,
    onApplyConfiguration: (layout: DeckDetailsLayout, kanaMode: Boolean) -> Unit
) {

    var selectedLayout by remember { mutableStateOf(layout) }
    var selectedKanaGroups by remember { mutableStateOf(kanaGroups) }

    BaseDialog(
        title = resolveString { letterDeckDetails.layoutDialog.title },
        onDismissRequest = onDismissRequest,
        onApplyClick = { onApplyConfiguration(selectedLayout, selectedKanaGroups) }
    ) {

        DeckDetailsLayout.values().forEach {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable { selectedLayout = it }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedLayout = it }) {
                    Icon(
                        imageVector = when (selectedLayout) {
                            it -> Icons.Default.RadioButtonChecked
                            else -> Icons.Default.RadioButtonUnchecked
                        },
                        contentDescription = null
                    )
                }
                Text(resolveString(it.titleResolver))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 10.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable { selectedKanaGroups = !selectedKanaGroups }
                .padding(start = 20.dp, end = 10.dp)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = resolveString { letterDeckDetails.layoutDialog.kanaGroupsTitle })
                Text(
                    text = resolveString { letterDeckDetails.layoutDialog.kanaGroupsSubtitle },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(checked = selectedKanaGroups, onCheckedChange = { selectedKanaGroups = it })
        }

    }

}


@Composable
private fun BaseDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onApplyClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        content = content,
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(text = resolveString { letterDeckDetails.dialogCommon.buttonCancel })
            }
            TextButton(onClick = onApplyClick) {
                Text(text = resolveString { letterDeckDetails.dialogCommon.buttonApply })
            }
        }
    )

}

@Composable
private fun SelectableRow(
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {

    val rippleColor = LocalRippleTheme.current.defaultColor()
    val rippleAlpha = LocalRippleTheme.current.rippleAlpha()

    val rowColor by animateColorAsState(
        targetValue = when (isSelected) {
            true -> rippleColor.copy(alpha = rippleAlpha.pressedAlpha)
            false -> rippleColor.copy(alpha = 0f)
        }
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(rowColor)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )

}
