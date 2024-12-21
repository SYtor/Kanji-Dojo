package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic

@Composable
fun SettingsScreen(
    mainNavigationState: MainNavigationState,
    viewModel: SettingsScreenContract.ViewModel = getMultiplatformViewModel()
) {

    SettingsScreenUI(
        state = viewModel.state.collectAsState(),
        onBackupButtonClick = {
            mainNavigationState.navigate(MainDestination.Backup)
        },
        onAccountButtonClick = {
            mainNavigationState.navigate(MainDestination.Account())
        },
        onSyncButtonClick = {
            mainNavigationState.navigate(MainDestination.Sync)
        },
        onFeedbackButtonClick = {
            mainNavigationState.navigate(MainDestination.Feedback(FeedbackTopic.General))
        },
        onAboutButtonClick = {
            mainNavigationState.navigate(MainDestination.About)
        },
        loadedContent = { screenState ->
            screenState.items.forEach { it.content(mainNavigationState) }
        }
    )

}