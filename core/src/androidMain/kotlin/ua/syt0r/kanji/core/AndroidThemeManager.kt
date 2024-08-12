package ua.syt0r.kanji.core

import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.theme_manager.ThemeManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTheme

class AndroidThemeManager(
    userPreferencesRepository: UserPreferencesRepository
) : ThemeManager(userPreferencesRepository) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        coroutineScope.launch { invalidate() }
    }

    override suspend fun changeTheme(theme: PreferencesTheme) {
        super.changeTheme(theme)
        applyThemeToActivity()
    }

    private suspend fun applyThemeToActivity() = withContext(Dispatchers.Main) {
        AppCompatDelegate.setDefaultNightMode(currentTheme.value.toUIMode())
    }

    private fun PreferencesTheme.toUIMode(): Int {
        return when (this) {
            PreferencesTheme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            PreferencesTheme.Light -> AppCompatDelegate.MODE_NIGHT_NO
            PreferencesTheme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
        }
    }

}