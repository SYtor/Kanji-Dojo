package ua.syt0r.kanji.presentation.screen.main.screen.sync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.common.rememberUrlHandler
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.DiscordInviteUrl

@Composable
fun SyncScreen(
    mainNavigationState: MainNavigationState,
    viewModel: SyncScreenContract.ViewModel = getMultiplatformViewModel()
) {

    val urlHandler = rememberUrlHandler()

    SyncScreenUI(
        state = viewModel.state.collectAsState(),
        onUpClick = mainNavigationState::navigateBack,
        navigateToAccountScreen = { mainNavigationState.navigate(MainDestination.Account()) },
        navigateToDiscord = { urlHandler.openInBrowser(DiscordInviteUrl) },
        sync = viewModel::sync
    )

}