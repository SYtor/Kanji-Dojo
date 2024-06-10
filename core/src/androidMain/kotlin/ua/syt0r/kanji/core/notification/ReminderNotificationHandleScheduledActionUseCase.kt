package ua.syt0r.kanji.core.notification

import android.app.ActivityManager
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import kotlin.math.max
import kotlin.math.min

class ReminderNotificationHandleScheduledActionUseCase(
    private val activityManager: ActivityManager,
    private val letterSrsManager: LetterSrsManager,
    private val notificationManager: ReminderNotificationContract.Manager,
    private val repository: UserPreferencesRepository,
    private val scheduler: ReminderNotificationContract.Scheduler,
    private val analyticsManager: AnalyticsManager
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

        val srsData = letterSrsManager.getUpdatedData()

        val maxLearn = srsData.decks.flatMap { it.writingDetails.new }.distinct().size +
                srsData.decks.flatMap { it.readingDetails.new }.distinct().size

        val maxReview = srsData.decks.flatMap { it.writingDetails.review }.distinct().size +
                srsData.decks.flatMap { it.readingDetails.review }.distinct().size

        val learnLeft = srsData.run {
            max(
                a = 0,
                b = min(dailyGoalConfiguration.learnLimit, maxLearn) - dailyProgress.studied
            )
        }

        val reviewLeft = srsData.run {
            max(
                a = 0,
                b = min(dailyGoalConfiguration.reviewLimit, maxReview) - dailyProgress.reviewed
            )
        }

        Logger.d("Preparing to show notification: learn[$maxLearn/$learnLeft] review[$maxReview/$reviewLeft]")
        if (learnLeft > 0 || reviewLeft > 0) {
            notificationManager.showNotification(learnLeft, reviewLeft)
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