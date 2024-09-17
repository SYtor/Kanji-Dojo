package ua.syt0r.kanji.core.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ua.syt0r.kanji.presentation.common.resources.string.getStrings

class ReminderNotificationManager(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat
) : ReminderNotificationContract.Manager {

    companion object {
        private const val ReminderNotificationChannelId = "reminder_notification_channel"
        private const val ReminderNotificationId = 1
    }

    override fun showNotification() {
        showNotificationInternal(
            message = getStrings().reminderNotification.noDetailsMessage
        )
    }

    override fun showNotification(new: Int, due: Int) {
        val strings = getStrings().reminderNotification
        showNotificationInternal(
            message = when {
                new == 0 -> strings.dueOnlyMessage(due)
                due == 0 -> strings.newOnlyMessage(new)
                else -> strings.message(new, due)
            }
        )
    }

    override fun dismissNotification() {
        notificationManager.cancel(ReminderNotificationId)
    }

    @SuppressLint("MissingPermission")
    private fun showNotificationInternal(message: String) {
        val strings = getStrings().reminderNotification

        val channel = NotificationChannelCompat
            .Builder(ReminderNotificationChannelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(strings.channelName)
            .build()

        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, ReminderNotificationChannelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(strings.title)
            .setContentText(message)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    context.packageManager.getLaunchIntentForPackage(context.packageName),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .setAutoCancel(true)
            .build()

        notificationManager.notify(ReminderNotificationId, notification)
    }

}