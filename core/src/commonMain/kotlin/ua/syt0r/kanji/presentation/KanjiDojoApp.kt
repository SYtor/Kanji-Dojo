package ua.syt0r.kanji.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.theme_manager.LocalThemeManager
import ua.syt0r.kanji.core.theme_manager.ThemeManager
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.screen.main.DeepLinkHandler
import ua.syt0r.kanji.presentation.screen.main.MainScreen

@Composable
fun KanjiDojoApp(
    deepLinkHandler: DeepLinkHandler = koinInject(),
    themeManager: ThemeManager = koinInject()
) {
    CompositionLocalProvider(LocalThemeManager provides themeManager) {
        AppTheme {
            Surface {
                Box(
                    modifier = Modifier.safeDrawingPadding()
                ) {
                    MainScreen(deepLinkHandler)
                }
            }
        }
    }
}
