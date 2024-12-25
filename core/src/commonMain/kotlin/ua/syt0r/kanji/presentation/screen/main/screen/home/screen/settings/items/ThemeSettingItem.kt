package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.theme_manager.LocalThemeManager
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTheme
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsPreferencePickerDialog
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum

enum class DisplayableTheme(
    val prefType: PreferencesTheme,
    override val titleResolver: StringResolveScope<String>
) : DisplayableEnum {

    System(PreferencesTheme.System, { settings.themeSystem }),
    Light(PreferencesTheme.Light, { settings.themeLight }),
    Dark(PreferencesTheme.Dark, { settings.themeDark });

    companion object {
        fun from(prefType: PreferencesTheme): DisplayableTheme =
            entries.first { it.prefType == prefType }
    }

}

object ThemeSettingItem : SettingsScreenContract.ListItem {

    @Composable
    override fun content(mainNavigationState: MainNavigationState) {

        val coroutineScope = rememberCoroutineScope()
        var showPicker by rememberSaveable { mutableStateOf(false) }

        val themeManager = LocalThemeManager.current
        val currentTheme = DisplayableTheme.from(themeManager.currentTheme.value)

        ListItem(
            headlineContent = { Text(resolveString { settings.themeTitle }) },
            supportingContent = { Text(resolveString(currentTheme.titleResolver)) },
            modifier = Modifier.clip(MaterialTheme.shapes.medium)
                .fillMaxWidth()
                .clickable { showPicker = true },
        )

        if (showPicker) {
            SettingsPreferencePickerDialog(
                onDismissRequest = { showPicker = false },
                title = resolveString { settings.themeTitle },
                options = DisplayableTheme.entries,
                defaultSelected = currentTheme,
                onSelected = { coroutineScope.launch { themeManager.changeTheme(it.prefType) } }
            )
        }

    }

}