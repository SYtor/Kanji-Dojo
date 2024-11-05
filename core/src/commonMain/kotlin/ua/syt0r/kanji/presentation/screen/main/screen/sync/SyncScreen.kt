package ua.syt0r.kanji.presentation.screen.main.screen.sync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
fun SyncScreen(
    mainNavigationState: MainNavigationState,
    viewModel: SyncScreenContract.ViewModel = getMultiplatformViewModel()
) {

    SyncScreenUI(
        state = viewModel.state.collectAsState(),
        onUpClick = mainNavigationState::navigateBack
    )

}