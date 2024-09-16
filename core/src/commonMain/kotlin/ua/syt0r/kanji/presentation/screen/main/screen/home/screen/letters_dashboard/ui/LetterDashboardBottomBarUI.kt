package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardBottomBar
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract.ScreenState


@Composable
fun LetterDashboardBottomBarUI(
    state: State<ScreenState>,
    navigateToDeckPicker: () -> Unit,
    modifier: Modifier
) {

    AnimatedContent(
        targetState = state.value,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier.fillMaxWidth()
    ) {

        when (it) {
            is ScreenState.Loaded -> {
                DeckDashboardBottomBar(
                    items = it.practiceTypeItems,
                    selectedItem = it.selectedPracticeTypeItem,
                    onFabClick = navigateToDeckPicker
                )
            }

            ScreenState.Loading -> Box(Modifier.fillMaxWidth())
        }

    }
}
