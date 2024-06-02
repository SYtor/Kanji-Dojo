package ua.syt0r.kanji.presentation.screen.settings

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.notification.ReminderNotificationConfiguration
import ua.syt0r.kanji.core.notification.ReminderNotificationContract
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.settings.GooglePlaySettingsScreenContract.ScreenState

class GooglePlaySettingsViewModel(
    private val viewModelScope: CoroutineScope,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analyticsManager: AnalyticsManager,
    private val reminderScheduler: ReminderNotificationContract.Scheduler
) : GooglePlaySettingsScreenContract.ViewModel {

    override val state = mutableStateOf<ScreenState>(ScreenState.Loading)

    override fun refresh() {
        viewModelScope.launch {
            state.value = ScreenState.Loaded(
                reminderConfiguration = ReminderNotificationConfiguration(
                    enabled = userPreferencesRepository.reminderEnabled.get(),
                    time = userPreferencesRepository.reminderTime.get()
                ),
                analyticsEnabled = userPreferencesRepository.analyticsEnabled.get()
            )
        }
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("settings")
    }

    override fun updateReminder(configuration: ReminderNotificationConfiguration) {
        val currentState = state.value as ScreenState.Loaded
        viewModelScope.launch {
            state.value = currentState.copy(reminderConfiguration = configuration)
            userPreferencesRepository.reminderEnabled.set(configuration.enabled)
            userPreferencesRepository.reminderTime.set(configuration.time)
            if (configuration.enabled) {
                reminderScheduler.scheduleNotification(configuration.time)
                analyticsManager.sendEvent("reminder_enabled") {
                    put("time", configuration.time.toString())
                }
            } else {
                reminderScheduler.unscheduleNotification()
                analyticsManager.sendEvent("reminder_disabled")
            }
        }
    }

    override fun updateAnalyticsEnabled(enabled: Boolean) {
        val currentState = state.value as ScreenState.Loaded
        viewModelScope.launch {
            state.value = currentState.copy(analyticsEnabled = enabled)
            userPreferencesRepository.analyticsEnabled.set(enabled)
            analyticsManager.setAnalyticsEnabled(enabled)
            analyticsManager.sendEvent("analytics_toggled") {
                put("analytics_enabled", enabled)
            }
        }
    }

}
