package ua.syt0r.kanji.presentation.screen.settings

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.notification.ReminderNotificationConfiguration
import ua.syt0r.kanji.core.notification.ReminderNotificationContract
import ua.syt0r.kanji.core.user_data.preferences.Preferences
import ua.syt0r.kanji.presentation.screen.settings.FdroidSettingsScreenContract.ScreenState

class FdroidSettingsViewModel(
    private val viewModelScope: CoroutineScope,
    private val appPreferences: Preferences.App,
    private val reminderScheduler: ReminderNotificationContract.Scheduler
) : FdroidSettingsScreenContract.ViewModel {

    override val state = mutableStateOf<ScreenState>(ScreenState.Loading)

    override fun refresh() {
        viewModelScope.launch {
            state.value = ScreenState.Loaded(
                reminderConfiguration = ReminderNotificationConfiguration(
                    enabled = appPreferences.reminderEnabled.get(),
                    time = appPreferences.reminderTime.get()
                )
            )
        }
    }

    override fun updateReminder(configuration: ReminderNotificationConfiguration) {
        viewModelScope.launch {
            state.value = ScreenState.Loaded(configuration)
            appPreferences.reminderEnabled.set(configuration.enabled)
            appPreferences.reminderTime.set(configuration.time)
            if (configuration.enabled) {
                reminderScheduler.scheduleNotification(configuration.time)
            } else {
                reminderScheduler.unscheduleNotification()
            }
        }
    }

}
