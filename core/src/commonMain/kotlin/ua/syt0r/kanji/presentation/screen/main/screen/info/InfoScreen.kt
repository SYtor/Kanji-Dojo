package ua.syt0r.kanji.presentation.screen.main.screen.info

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState


@Composable
fun InfoScreen(
    screenData: InfoScreenData,
    mainNavigationState: MainNavigationState,
    viewModel: InfoScreenContract.ViewModel = getMultiplatformViewModel(screenData)
) {

    val clipboardManager = LocalClipboardManager.current

    InfoScreenUI(
        state = viewModel.state,
        onUpButtonClick = { mainNavigationState.navigateBack() },
        onCopyButtonClick = {
//            clipboardManager.setText(AnnotatedString(letter))
        },
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