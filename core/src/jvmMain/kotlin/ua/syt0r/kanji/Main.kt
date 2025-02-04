package ua.syt0r.kanji

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import ua.syt0r.kanji.di.appModules
import ua.syt0r.kanji.presentation.KanjiDojoApp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.Orientation

fun main(args: Array<String>) = application {
    startKoin { loadKoinModules(appModules) }

    val windowState = rememberWindowState()

    Window(
        onCloseRequest = { exitApplication() },
        state = windowState,
        title = resolveString { appName },
        icon = painterResource("icon.png")
    ) {

        val orientation = when (windowState.size.run { height > width }) {
            true -> Orientation.Portrait
            false -> Orientation.Landscape
        }

        KanjiDojoApp(orientation)

    }
}
