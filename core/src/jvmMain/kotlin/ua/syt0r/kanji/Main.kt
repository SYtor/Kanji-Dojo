package ua.syt0r.kanji

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import ua.syt0r.kanji.core.sync.SyncManager
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.di.appModules
import ua.syt0r.kanji.presentation.KanjiDojoApp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString

fun main(args: Array<String>) = application {
    val koinApplication = startKoin { loadKoinModules(appModules) }

    val windowState = rememberWindowState()

    val icon = painterResource("icon.png")
    val density = LocalDensity.current

    val syncManager = koinApplication.koin.get<SyncManager>()
    val coroutineScope = rememberCoroutineScope()

    Window(
        onCloseRequest = {
            if (syncManager.state.value is SyncState.Disabled) {
                exitApplication()
                return@Window
            }

            coroutineScope.launch {
                syncManager.state
                    .onStart { syncManager.forceSync() }
                    .filter { it is SyncState.NoChanges || it is SyncState.Canceled }
                    .take(1)
                    .collect()
                exitApplication()
            }
        },
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

