package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import androidx.compose.runtime.Composable

@Composable
fun JvmSettingsScreenUI(
    onBackupButtonClick: () -> Unit,
    onAccountButtonClick: () -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onAboutButtonClick: () -> Unit
) {

    SettingsContent {

        SettingsThemeToggle()

        SettingsBackupButton(onBackupButtonClick)

        SettingsAccountButton(onAccountButtonClick)

        SettingsFeedbackButton(onFeedbackButtonClick)

        SettingsAboutButton(onAboutButtonClick)

    }

}