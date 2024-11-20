package ua.syt0r.kanji.presentation.screen.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import ua.syt0r.kanji.core.notification.ReminderNotificationConfiguration
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsAboutButton
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsAccountButton
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsBackupButton
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsContent
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsFeedbackButton
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsReminderNotification
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsSwitchRow
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsSyncButton
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsThemeToggle
import ua.syt0r.kanji.presentation.screen.settings.GooglePlaySettingsScreenContract.ScreenState

@Composable
fun GooglePlaySettingsScreenUI(
    state: State<ScreenState>,
    onReminderConfigurationChange: (ReminderNotificationConfiguration) -> Unit,
    onBackupButtonClick: () -> Unit,
    onAccountButtonClick: () -> Unit,
    onSyncButtonClick: () -> Unit,
    onFeedbackButtonClick: () -> Unit,
    onAboutButtonClick: () -> Unit,
    onAnalyticsToggled: (Boolean) -> Unit
) {

    val transition = updateTransition(targetState = state.value, label = "Content Transition")
    transition.AnimatedContent(
        contentKey = { it::class },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = Modifier.fillMaxSize()
    ) {

        when (it) {
            is ScreenState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize()
                )
            }

            is ScreenState.Loaded -> {

                SettingsContent {

                    val strings = resolveString { settings }

                    SettingsSwitchRow(
                        title = strings.analyticsTitle,
                        message = strings.analyticsMessage,
                        isEnabled = it.analyticsEnabled,
                        onToggled = { onAnalyticsToggled(!it.analyticsEnabled) }
                    )

                    SettingsReminderNotification(
                        configuration = it.reminderConfiguration,
                        onChanged = onReminderConfigurationChange
                    )

                    SettingsThemeToggle()

                    SettingsBackupButton(onBackupButtonClick)

                    SettingsAccountButton(onAccountButtonClick)

                    SettingsSyncButton(onSyncButtonClick)

                    SettingsFeedbackButton(onFeedbackButtonClick)

                    SettingsAboutButton(onAboutButtonClick)

                }

            }

        }

    }

}
