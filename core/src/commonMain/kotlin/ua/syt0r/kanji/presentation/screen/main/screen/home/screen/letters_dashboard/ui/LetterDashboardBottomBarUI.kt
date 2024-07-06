package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract.ScreenState
import kotlin.math.max


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LetterDashboardBottomBarUI(
    state: State<ScreenState>,
    navigateToDeckPicker: () -> Unit,
    updateConfiguration: (DailyGoalConfiguration) -> Unit,
    modifier: Modifier
) {

    AnimatedContent(
        targetState = state.value,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.fillMaxWidth()
    ) {

        when (it) {
            is ScreenState.Loaded -> {
                Layout(
                    modifier = Modifier.fillMaxWidth(),
                    content = {
                        LettersDashboardDailyLimitIndicator(
                            data = it.dailyIndicatorData,
                            updateConfiguration = updateConfiguration
                        )
                        FloatingActionButton(
                            onClick = navigateToDeckPicker,
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

            ScreenState.Loading -> Box(Modifier.fillMaxWidth())
        }

    }
}
