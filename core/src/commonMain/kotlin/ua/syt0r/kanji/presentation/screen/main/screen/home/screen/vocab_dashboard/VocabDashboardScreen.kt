package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
fun VocabDashboardScreen(
    mainNavigationState: MainNavigationState,
    viewModel: VocabDashboardScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.reportScreenShown()
    }

    VocabDashboardScreenUI(
        state = viewModel.state.collectAsState(),
        select = { viewModel.select(it) },
        navigateToPractice = {
            mainNavigationState.navigate(
                MainDestination.VocabPractice(expressionIds = it.expressionIds)
            )
        }
    )

}