package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.theme_manager.LocalThemeManager
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTheme
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation

@Composable
fun SettingsContent(
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentWidth()
            .verticalScroll(rememberScrollState())
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp)
    ) {

        val orientation = LocalOrientation.current
        if (orientation == Orientation.Landscape) {
            Spacer(Modifier.height(20.dp))
        }

        content()

    }

}

@Composable
fun SettingsSwitchRow(
    title: String,
    message: String,
    isEnabled: Boolean,
    onToggled: () -> Unit
) {

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onToggled)
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .widthIn(min = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Column(
            modifier = Modifier.weight(1f).padding(vertical = 10.dp)
        ) {
            Text(text = title)
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggled() },
            colors = SwitchDefaults.colors(
                uncheckedTrackColor = MaterialTheme.colorScheme.background
            )
        )
    }

}

@Composable
fun SettingsThemeToggle() {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .padding(start = 20.dp, end = 10.dp)
            .widthIn(min = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = resolveString { settings.themeTitle },
            modifier = Modifier.weight(1f).padding(vertical = 8.dp)
        )

        var isExpanded by remember { mutableStateOf(false) }

        Box {

            val themeManager = LocalThemeManager.current
            val coroutineScope = rememberCoroutineScope()

            TextButton(
                onClick = { isExpanded = !isExpanded },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(themeManager.currentTheme.value.resolveDisplayText())
                Icon(Icons.Default.ArrowDropDown, null)
            }

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                PreferencesTheme.values().forEach {
                    DropdownMenuItem(
                        onClick = { coroutineScope.launch { themeManager.changeTheme(it) } },
                        text = { Text(text = it.resolveDisplayText()) }
                    )
                }
            }
        }

    }
}

@Composable
fun SettingsBackupButton(onClick: () -> Unit) {
    SettingsTextButton(
        text = resolveString { settings.backupTitle },
        onClick = onClick
    )
}

@Composable
fun SettingsAccountButton(onClick: () -> Unit) {
    SettingsTextButton(
        text = resolveString { "Account" },
        onClick = onClick
    )
}

@Composable
fun SettingsFeedbackButton(onClick: () -> Unit) {
    SettingsTextButton(
        text = resolveString { settings.feedbackTitle },
        onClick = onClick
    )
}

@Composable
fun SettingsAboutButton(onClick: () -> Unit) {
    SettingsTextButton(
        text = resolveString { settings.aboutTitle },
        onClick = onClick
    )
}

@Composable
private fun SettingsTextButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .heightIn(min = 30.dp)
            .wrapContentHeight(),
    )
}

@Composable
private fun PreferencesTheme.resolveDisplayText(): String = resolveString {
    when (this@resolveDisplayText) {
        PreferencesTheme.System -> settings.themeSystem
        PreferencesTheme.Light -> settings.themeLight
        PreferencesTheme.Dark -> settings.themeDark
    }
}
