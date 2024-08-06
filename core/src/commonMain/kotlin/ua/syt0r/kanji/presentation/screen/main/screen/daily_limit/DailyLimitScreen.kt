package ua.syt0r.kanji.presentation.screen.main.screen.daily_limit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState


@Composable
fun DailyLimitScreen(
    mainNavigationState: MainNavigationState,
    viewModel: DailyLimitScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.reportScreenShown()
    }

    DailyLimitScreenUI(
        state = viewModel.state.collectAsState(),
        navigateBack = { mainNavigationState.navigateBack() },
        saveChanges = { viewModel.saveChanges() }
    )

}
