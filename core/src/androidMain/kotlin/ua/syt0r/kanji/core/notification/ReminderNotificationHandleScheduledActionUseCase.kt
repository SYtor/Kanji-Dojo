package ua.syt0r.kanji.core.notification

import android.app.ActivityManager
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository

class ReminderNotificationHandleScheduledActionUseCase(
    private val activityManager: ActivityManager,
    private val letterSrsManager: LetterSrsManager,
    private val notificationManager: ReminderNotificationContract.Manager,
    private val repository: UserPreferencesRepository,
    private val scheduler: ReminderNotificationContract.Scheduler,
    private val analyticsManager: AnalyticsManager,
) : ReminderNotificationContract.HandleScheduledActionUseCase {

    override suspend fun handle() {
        showNotification()
        scheduleNextNotification()
    }

    private suspend fun showNotification() {
        val isInForeground = activityManager.appTasks.isNotEmpty()
        if (isInForeground) return

        if (!repository.dailyLimitEnabled.get()) {
            notificationManager.showNotification()
            analyticsManager.sendEvent("showing_notification_no_limit")
            return
        }

        val srsData = letterSrsManager.getDecks()
        val dailyProgress = srsData.dailyProgress
        val newLeft = dailyProgress.leftoversMap.map { it.value.new }.sum()
        val dueLeft = dailyProgress.leftoversMap.map { it.value.due }.sum()

        Logger.d("Preparing to show notification: dailyProgress[$dailyProgress]")
        if (newLeft > 0 || dueLeft > 0) {
            notificationManager.showNotification(
                learn = newLeft,
                review = dueLeft
            )
            analyticsManager.sendEvent("showing_notification")
        } else {
            analyticsManager.sendEvent("showing_notification_but_daily_goal_met")
        }
    }

    private suspend fun scheduleNextNotification() {
        val time = repository.reminderTime.get()
        scheduler.scheduleNotification(time)
    }

}