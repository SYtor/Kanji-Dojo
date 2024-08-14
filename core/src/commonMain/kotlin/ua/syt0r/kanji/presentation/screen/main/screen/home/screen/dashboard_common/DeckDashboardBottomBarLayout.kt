package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import kotlin.math.max


@Composable
fun DeckDashboardBottomBarLayout(
    centralContent: @Composable () -> Unit,
    fabContent: @Composable () -> Unit,
    modifier: Modifier
) {

    Layout(
        modifier = modifier,
        content = {
            centralContent()
            fabContent()
        }
    ) { measurables, constraints ->
        val fabSpacing = 20.dp.roundToPx()
        val indicatorPlaceable = measurables.first()
            .measure(constraints.copy(minWidth = 0))
        val fabPlaceable = measurables[1].measure(constraints.copy(minWidth = 0))

        val width = constraints.maxWidth
        val fitInLine =
            (width / 2 + indicatorPlaceable.width / 2 + fabPlaceable.width + fabSpacing) > width

        val height = when (fitInLine) {
            true -> indicatorPlaceable.height + fabPlaceable.height
            false -> max(indicatorPlaceable.height, fabPlaceable.height) + fabSpacing
        }

        layout(
            width = width,
            height = height
        ) {

            fabPlaceable.place(
                x = constraints.maxWidth - fabPlaceable.width - fabSpacing,
                y = when (fitInLine) {
                    true -> 0
                    false -> height - fabPlaceable.height - fabSpacing
                }
            )

            indicatorPlaceable.place(
                x = width / 2 - indicatorPlaceable.width / 2,
                y = height - indicatorPlaceable.height
            )

        }
    }
}
