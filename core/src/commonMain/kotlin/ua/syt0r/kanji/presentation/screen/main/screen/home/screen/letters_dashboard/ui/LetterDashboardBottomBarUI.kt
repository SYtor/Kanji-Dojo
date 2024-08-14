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
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardBottomBarLayout
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract.ScreenState


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LetterDashboardBottomBarUI(
    state: State<ScreenState>,
    navigateToDeckPicker: () -> Unit,
    onDailyLimitIndicatorClick: () -> Unit,
    modifier: Modifier
) {

    AnimatedContent(
        targetState = state.value,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.fillMaxWidth()
    ) {

        when (it) {
            is ScreenState.Loaded -> {
                DeckDashboardBottomBarLayout(
                    modifier = Modifier.fillMaxWidth(),
                    centralContent = {
                        LettersDashboardDailyLimitIndicator(
                            data = it.dailyIndicatorData,
                            onIndicatorClick = onDailyLimitIndicatorClick
                        )
                    },
                    fabContent = {
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
                )
            }

            ScreenState.Loading -> Box(Modifier.fillMaxWidth())
        }

    }
}
