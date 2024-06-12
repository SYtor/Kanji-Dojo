package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState

@Composable
fun StatsScreen(
    viewModel: StatsScreenContract.ViewModel
) {

    LaunchedEffect(Unit) {
        viewModel.reportScreenShown()
    }

    StatsScreenUI(state = viewModel.state.collectAsState())

}