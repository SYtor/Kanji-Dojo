package ua.syt0r.kanji.core.theme_manager

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.runBlocking
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesTheme


open class ThemeManager(
    private val appPreferences: PreferencesContract.AppPreferences
) {

    private val mCurrentTheme = mutableStateOf(
        value = runBlocking { appPreferences.theme.get() }
    )

    val currentTheme: State<PreferencesTheme> = mCurrentTheme

    open suspend fun changeTheme(theme: PreferencesTheme) {
        mCurrentTheme.value = theme
        appPreferences.theme.set(theme)
    }

    suspend fun invalidate() {
        changeTheme(appPreferences.theme.get())
    }

    val isDarkTheme: Boolean
        @Composable
        get() = when (currentTheme.value) {
            PreferencesTheme.System -> isSystemInDarkTheme()
            PreferencesTheme.Light -> false
            PreferencesTheme.Dark -> true
        }

}

val LocalThemeManager = compositionLocalOf<ThemeManager> {
    throw IllegalStateException("ThemeManager is not initialized")
}