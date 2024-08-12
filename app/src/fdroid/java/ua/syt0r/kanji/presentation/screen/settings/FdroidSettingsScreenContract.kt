package ua.syt0r.kanji.presentation.screen.settings

import androidx.compose.runtime.State
import ua.syt0r.kanji.core.notification.ReminderNotificationConfiguration

interface FdroidSettingsScreenContract {

    interface ViewModel {

        val state: State<ScreenState>

        fun refresh()
        fun updateReminder(configuration: ReminderNotificationConfiguration)

    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Loaded(
            val reminderConfiguration: ReminderNotificationConfiguration
        ) : ScreenState

    }

}