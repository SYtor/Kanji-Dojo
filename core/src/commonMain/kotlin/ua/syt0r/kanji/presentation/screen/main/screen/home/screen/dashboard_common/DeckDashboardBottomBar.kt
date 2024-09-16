package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import kotlin.math.max

data class DeckDashboardPracticeTypeItem<T : ScreenPracticeType>(
    val practiceType: T,
    val hasPendingReviews: Boolean
)

typealias LetterDeckDashboardPracticeTypeItem = DeckDashboardPracticeTypeItem<ScreenLetterPracticeType>
typealias VocabDeckDashboardPracticeTypeItem = DeckDashboardPracticeTypeItem<ScreenVocabPracticeType>

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T : ScreenPracticeType> AnimatedContentScope.DeckDashboardBottomBar(
    items: List<DeckDashboardPracticeTypeItem<T>>,
    selectedItem: MutableState<DeckDashboardPracticeTypeItem<T>>,
    onFabClick: () -> Unit
) {
    DeckDashboardBottomBarLayout(
        modifier = Modifier.fillMaxWidth(),
        centralContent = {
            var dropdownExpanded by remember { mutableStateOf(false) }
            val item = selectedItem.value
            Box {
                Row(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(onClick = { dropdownExpanded = true })
                        .padding(ButtonDefaults.TextButtonContentPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val practiceTypeLabel = resolveString(item.practiceType.titleResolver)
                    Text(
                        text = resolveString {
                            commonDashboard.selectedPracticeTypeTemplate(practiceTypeLabel)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Light
                    )
                    Icon(Icons.Default.ArrowDropDown, null)
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {

                    items.forEach { dropDownItem ->
                        PracticeTypeDropdownItem(
                            practiceType = dropDownItem.practiceType,
                            showIndicator = dropDownItem.hasPendingReviews,
                            onClick = {
                                selectedItem.value = dropDownItem
                                dropdownExpanded = false
                            }
                        )
                    }

                }
            }
        },
        fabContent = {
            FloatingActionButton(
                onClick = onFabClick,
                modifier = Modifier.animateEnterExit(
                    enter = scaleIn(),
                    exit = scaleOut()
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    )
}

private val ExtraBackgroundVerticalPadding = 4.dp

@Composable
private fun DeckDashboardBottomBarLayout(
    centralContent: @Composable () -> Unit,
    fabContent: @Composable () -> Unit,
    modifier: Modifier
) {

    Layout(
        modifier = modifier,
        content = {
            Box(
                modifier = Modifier.padding(vertical = ExtraBackgroundVerticalPadding)
            ) {
                centralContent()
            }

            fabContent()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(false) { } // Prevents clicks from passing through
            ) {
                HorizontalDivider(Modifier.width(380.dp).align(Alignment.TopCenter))
            }
        }
    ) { measurables, constraints ->

        val fabPadding = 20.dp.roundToPx()

        val indicatorPlaceable = measurables[0].measure(constraints.copy(minWidth = 0))
        val fabPlaceable = measurables[1].measure(constraints.copy(minWidth = 0))
        val backgroundPlaceable = measurables[2].measure(
            constraints.copy(maxHeight = indicatorPlaceable.height)
        )

        val availableWidth = constraints.maxWidth
        val minWidthIfSingleLine = availableWidth / 2 + indicatorPlaceable.width / 2 +
                fabPlaceable.width + fabPadding * 2

        val fitsSingleLine = availableWidth > minWidthIfSingleLine

        val height: Int = when {
            fitsSingleLine -> max(indicatorPlaceable.height, fabPlaceable.height + fabPadding)
            else -> indicatorPlaceable.height + fabPadding + fabPlaceable.height
        }

        val indicatorY = height - indicatorPlaceable.height

        layout(
            width = availableWidth,
            height = height
        ) {

            backgroundPlaceable.place(
                x = 0,
                y = indicatorY
            )

            fabPlaceable.place(
                x = constraints.maxWidth - fabPlaceable.width - fabPadding,
                y = 0
            )

            indicatorPlaceable.place(
                x = availableWidth / 2 - indicatorPlaceable.width / 2,
                y = indicatorY
            )

        }
    }
}
