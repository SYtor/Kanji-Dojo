package ua.syt0r.kanji.presentation.screen.settings

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.notification.ReminderNotificationConfiguration
import ua.syt0r.kanji.core.notification.ReminderNotificationContract
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.settings.FdroidSettingsScreenContract.ScreenState

class FdroidSettingsViewModel(
    private val viewModelScope: CoroutineScope,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: ReminderNotificationContract.Scheduler
) : FdroidSettingsScreenContract.ViewModel {

    override val state = mutableStateOf<ScreenState>(ScreenState.Loading)

    override fun refresh() {
        viewModelScope.launch {
            state.value = ScreenState.Loaded(
                reminderConfiguration = ReminderNotificationConfiguration(
                    enabled = userPreferencesRepository.reminderEnabled.get(),
                    time = userPreferencesRepository.reminderTime.get()
                )
            )
        }
    }

    override fun updateReminder(configuration: ReminderNotificationConfiguration) {
        viewModelScope.launch {
            state.value = ScreenState.Loaded(configuration)
            userPreferencesRepository.reminderEnabled.set(configuration.enabled)
            userPreferencesRepository.reminderTime.set(configuration.time)
            if (configuration.enabled) {
                reminderScheduler.scheduleNotification(configuration.time)
            } else {
                reminderScheduler.unscheduleNotification()
            }
        }
    }

}
