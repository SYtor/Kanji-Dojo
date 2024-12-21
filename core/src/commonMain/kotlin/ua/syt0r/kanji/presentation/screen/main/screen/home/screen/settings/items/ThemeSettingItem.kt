package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.items

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract


object ThemeSettingItem : SettingsScreenContract.ListItem {

    @Composable
    override fun content(mainNavigationState: MainNavigationState) {
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

}

@Composable
private fun PreferencesTheme.resolveDisplayText(): String = resolveString {
    when (this@resolveDisplayText) {
        PreferencesTheme.System -> settings.themeSystem
        PreferencesTheme.Light -> settings.themeLight
        PreferencesTheme.Dark -> settings.themeDark
    }
}