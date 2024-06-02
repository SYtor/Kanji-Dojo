package ua.syt0r.kanji.core

import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.theme_manager.ThemeManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.core.user_data.preferences.SupportedTheme

class AndroidThemeManager(
    userPreferencesRepository: UserPreferencesRepository
) : ThemeManager(userPreferencesRepository) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        coroutineScope.launch { invalidate() }
    }

    override suspend fun changeTheme(theme: SupportedTheme) {
        super.changeTheme(theme)
        applyThemeToActivity()
    }

    private suspend fun applyThemeToActivity() = withContext(Dispatchers.Main) {
        AppCompatDelegate.setDefaultNightMode(currentTheme.value.toUIMode())
    }

    private fun SupportedTheme.toUIMode(): Int {
        return when (this) {
            SupportedTheme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            SupportedTheme.Light -> AppCompatDelegate.MODE_NIGHT_NO
            SupportedTheme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
        }
    }

}