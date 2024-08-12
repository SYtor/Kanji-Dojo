package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel

@Composable
fun StatsScreen(
    viewModel: StatsScreenContract.ViewModel = getMultiplatformViewModel()
) {

    StatsScreenUI(state = viewModel.state.collectAsState())

}