package ua.syt0r.kanji

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import ua.syt0r.kanji.di.appModules
import ua.syt0r.kanji.presentation.KanjiDojoApp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString

fun main(args: Array<String>) = application {
    startKoin { loadKoinModules(appModules) }

    val windowState = rememberWindowState()

    val icon = painterResource("icon.png")
    val density = LocalDensity.current

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = resolveString { appName },
        icon = icon
    ) {

        SideEffect {
            // TODO low resolution icon workaround, remove when fixed https://github.com/JetBrains/compose-multiplatform/issues/1838
            window.iconImage = icon.toAwtImage(density, LayoutDirection.Ltr, Size(128f, 128f))
        }

        CompositionLocalProvider(LocalWindowState provides windowState) {
            KanjiDojoApp()
        }
    }
}

val LocalWindowState = compositionLocalOf<WindowState> {
    throw IllegalStateException("Window state not provided")
}

