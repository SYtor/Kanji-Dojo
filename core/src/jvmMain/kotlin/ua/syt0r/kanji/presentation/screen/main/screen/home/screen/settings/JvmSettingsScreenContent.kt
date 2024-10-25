package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import androidx.compose.runtime.Composable
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic

object JvmSettingsScreenContent : SettingsScreenContract.Content {

    @Composable
    override fun Draw(mainNavigationState: MainNavigationState) {
        JvmSettingsScreenUI(
            onBackupButtonClick = { mainNavigationState.navigate(MainDestination.Backup) },
            onAccountButtonClick = { mainNavigationState.navigate(MainDestination.Account) },
            onFeedbackButtonClick = {
                mainNavigationState.navigate(MainDestination.Feedback(FeedbackTopic.General))
            },
            onAboutButtonClick = { mainNavigationState.navigate(MainDestination.About) }
        )
    }

}