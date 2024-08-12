package ua.syt0r.kanji.presentation.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract

object FdroidSettingsScreenContent : SettingsScreenContract.Content {

    @Composable
    override fun Draw(
        mainNavigationState: MainNavigationState
    ) {

        val viewModel = getMultiplatformViewModel<FdroidSettingsScreenContract.ViewModel>()

        LaunchedEffect(Unit) {
            viewModel.refresh()
        }

        FdroidSettingsScreenUI(
            state = viewModel.state,
            onReminderConfigurationChange = { viewModel.updateReminder(it) },
            onBackupButtonClick = { mainNavigationState.navigate(MainDestination.Backup) },
            onFeedbackButtonClick = {
                mainNavigationState.navigate(
                    MainDestination.Feedback(FeedbackTopic.General)
                )
            },
            onAboutButtonClick = { mainNavigationState.navigate(MainDestination.About) }
        )

    }

}