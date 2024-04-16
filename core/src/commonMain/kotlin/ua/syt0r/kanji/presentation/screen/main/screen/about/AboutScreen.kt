package ua.syt0r.kanji.presentation.screen.main.screen.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ua.syt0r.kanji.presentation.common.rememberUrlHandler
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState


@Composable
fun AboutScreen(
    mainNavigationState: MainNavigationState,
    viewModel: AboutScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.reportScreenShown()
    }

    val urlHandler = rememberUrlHandler()

    AboutScreenUI(
        onUpButtonClick = { mainNavigationState.navigateBack() },
        openLink = { url ->
            urlHandler.openInBrowser(url)
            viewModel.reportUrlClick(url)
        }
    )

}
