package ua.syt0r.kanji.presentation.screen.main.screen.info

import androidx.compose.runtime.Composable
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState


@Composable
fun InfoScreen(
    screenData: InfoScreenData,
    mainNavigationState: MainNavigationState,
    viewModel: InfoScreenContract.ViewModel = getMultiplatformViewModel(screenData)
) {

    InfoScreenUI(
        state = viewModel.state,
        onUpButtonClick = { mainNavigationState.navigateBack() },
        onFuriganaClick = {
            val nextScreenData = InfoScreenData.Letter(it)
            if (screenData != nextScreenData)
                mainNavigationState.navigate(MainDestination.Info(nextScreenData))
        },
        onWordClick = {
            val nextScreenData = it.toInfoScreenData()
            if (screenData != nextScreenData)
                mainNavigationState.navigate(MainDestination.Info(nextScreenData))
        }
    )

}