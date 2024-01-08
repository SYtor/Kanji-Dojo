package ua.syt0r.kanji.presentation.screen.settings

import androidx.compose.runtime.State
import ua.syt0r.kanji.core.notification.ReminderNotificationConfiguration

interface FdroidSettingsScreenContract {

    interface ViewModel {

        val state: State<ScreenState>

        fun refresh()
        fun reportScreenShown()

        fun updateReminder(configuration: ReminderNotificationConfiguration)

        fun updateAltStrokeEvaluatorEnabled(enabled:Boolean)

    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Loaded(
            val reminderConfiguration: ReminderNotificationConfiguration,
            val altStrokeEvaluatorEnabled: Boolean
        ) : ScreenState

    }

}