package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalTime
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.notification.ReminderNotificationConfiguration
import ua.syt0r.kanji.core.notification.ReminderNotificationContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.getBottomLineShape
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState


class AndroidReminderSettingListItem(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val reminderScheduler: ReminderNotificationContract.Scheduler,
    private val analyticsManager: AnalyticsManager
) : SettingsScreenContract.ConfigurableListItem {

    private lateinit var configuration: MutableState<ReminderNotificationConfiguration>

    override suspend fun prepare(coroutineScope: CoroutineScope) {
        val initialValue = ReminderNotificationConfiguration(
            enabled = appPreferences.reminderEnabled.get(),
            time = appPreferences.reminderTime.get()
        )
        configuration = mutableStateOf(initialValue)

        snapshotFlow { configuration.value }.drop(1)
            .onEach { value ->
                appPreferences.reminderEnabled.set(value.enabled)
                appPreferences.reminderTime.set(value.time)
                if (value.enabled) {
                    reminderScheduler.scheduleNotification(value.time)
                    analyticsManager.sendEvent("reminder_enabled") {
                        put("time", value.time.toString())
                    }
                } else {
                    reminderScheduler.unscheduleNotification()
                    analyticsManager.sendEvent("reminder_disabled")
                }
            }
            .launchIn(coroutineScope)
    }

    @Composable
    override fun content(mainNavigationState: MainNavigationState) {
        SettingsReminderNotification(
            configuration = configuration.value,
            onChanged = { configuration.value = it }
        )
    }

}

@Composable
fun SettingsReminderNotification(
    configuration: ReminderNotificationConfiguration,
    onChanged: (ReminderNotificationConfiguration) -> Unit
) {

    var shouldShowReminderDialog by remember { mutableStateOf(false) }
    if (shouldShowReminderDialog) {
        ReminderDialog(
            configuration,
            onDismissRequest = { shouldShowReminderDialog = false },
            onChanged = {
                onChanged(it)
                shouldShowReminderDialog = false
            }
        )
    }

    val strings = resolveString { settings }
    val message = configuration.run {
        val statusMessage = if (enabled)
            strings.reminderEnabled
        else strings.reminderDisabled
        val timeMessage = time.prettyFormatted()
        listOf(statusMessage, timeMessage).joinToString()
    }

    SettingsTextButton(
        title = strings.reminderTitle,
        subtitle = message,
        onClick = { shouldShowReminderDialog = true }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("InlinedApi")
@Composable
private fun ReminderDialog(
    configuration: ReminderNotificationConfiguration,
    onDismissRequest: () -> Unit,
    onChanged: (ReminderNotificationConfiguration) -> Unit
) {
    val strings = resolveString { reminderDialog }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .height(IntrinsicSize.Max)
                .padding(
                    top = 20.dp,
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 10.dp
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = strings.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 10.dp),
            )

            var notificationEnabled by remember {
                mutableStateOf(configuration.enabled)
            }
            var textFieldValue by remember {
                mutableStateOf(TextFieldValue(configuration.time.prettyFormatted()))
            }

            val time = textFieldValue.parseTime()

            val context = LocalContext.current
            val hasNotificationPermission by context.isNotificationPermissionGranted()

            val permissionActivityLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = {}
            )

            if (!hasNotificationPermission) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.error,
                            MaterialTheme.shapes.medium
                        )
                        .padding(start = 20.dp, end = 10.dp)
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = strings.noPermissionLabel,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onError
                    )
                    TextButton(
                        onClick = {
                            val isPermissionsRequestDenied = !ActivityCompat
                                .shouldShowRequestPermissionRationale(
                                    context.findActivity()!!,
                                    POST_NOTIFICATIONS
                                )
                            if (isPermissionsRequestDenied) {
                                openNotificationSettings(context)
                            } else {
                                permissionActivityLauncher.launch(POST_NOTIFICATIONS)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(strings.noPermissionButton)
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .weight(1f),
            ) {

                Text(
                    text = strings.enabledLabel,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = notificationEnabled,
                    enabled = hasNotificationPermission,
                    onCheckedChange = { notificationEnabled = !notificationEnabled }
                )
            }

            TimeInput(state = rememberTimePickerState())

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .weight(1f)
            ) {
                Text(
                    text = strings.timeLabel,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                BasicTextField(
                    value = textFieldValue,
                    enabled = hasNotificationPermission,
                    onValueChange = { textFieldValue = it.timeFormatted() },
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(80.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.shapes.medium
                        )
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.run {
                                if (time == null) error else outline
                            },
                            shape = getBottomLineShape(2.dp)
                        )
                        .padding(4.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 10.dp)
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(strings.cancelButton)
                }
                TextButton(
                    onClick = {
                        val updatedConfiguration = ReminderNotificationConfiguration(
                            enabled = notificationEnabled,
                            time = time!!
                        )
                        onChanged(updatedConfiguration)
                    },
                    enabled = time != null
                ) {
                    Text(strings.applyButton)
                }
            }

        }
    }
}

@Composable
private fun Context.isNotificationPermissionGranted(): State<Boolean> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
        return remember { mutableStateOf(true) }

    val isGranted = remember {
        mutableStateOf(checkSelfPermission(POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
    }

    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)
    DisposableEffect(lifecycleOwner.value) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGranted.value = checkSelfPermission(POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED
            }
        }
        val lifecycle = lifecycleOwner.value.lifecycle
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    return isGranted
}

private fun LocalTime.prettyFormatted(): String {
    return "%02d:%02d".format(hour, minute)
}

private fun TextFieldValue.timeFormatted(): TextFieldValue {
    val updatedText = text.filter { it.isDigit() }.let {
        if (it.length > 2) "${it.take(2)}:${it.substring(2)}".take(5)
        else it
    }

    return copy(
        text = updatedText,
        selection = TextRange(updatedText.length)
    )
}

private fun TextFieldValue.parseTime(): LocalTime? {
    val text = text.filter { it.isDigit() }

    if (text.length != 4) return null

    val hour = text.take(2).toInt()
    if (hour > 23) return null

    val minute = text.takeLast(2).toInt()
    if (minute > 59) return null

    return LocalTime(hour, minute)
}

fun Context.findActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}


private fun openNotificationSettings(context: Context) {
    val intent = Intent()
    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    context.startActivity(intent)
}