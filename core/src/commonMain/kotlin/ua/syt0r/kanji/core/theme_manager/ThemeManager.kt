package ua.syt0r.kanji.core.theme_manager

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.runBlocking
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.core.user_data.preferences.SupportedTheme


open class ThemeManager(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val mCurrentTheme = mutableStateOf(
        value = runBlocking { userPreferencesRepository.theme.get() }
    )

    val currentTheme: State<SupportedTheme> = mCurrentTheme

    open suspend fun changeTheme(theme: SupportedTheme) {
        mCurrentTheme.value = theme
        userPreferencesRepository.theme.set(theme)
    }

    suspend fun invalidate() {
        changeTheme(userPreferencesRepository.theme.get())
    }

    val isDarkTheme: Boolean
        @Composable
        get() = when (currentTheme.value) {
            SupportedTheme.System -> isSystemInDarkTheme()
            SupportedTheme.Light -> false
            SupportedTheme.Dark -> true
        }

}

val LocalThemeManager = compositionLocalOf<ThemeManager> {
    throw IllegalStateException("ThemeManager is not initialized")
}