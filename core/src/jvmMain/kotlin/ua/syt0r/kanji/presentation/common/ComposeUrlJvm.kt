package ua.syt0r.kanji.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.net.URI

@Composable
actual fun rememberUrlHandler(): UrlHandler {
    return remember {
        object : UrlHandler {
            override fun openInBrowser(url: String) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(URI.create(url))
                } else {
                    ProcessBuilder("xdg-open", url).start()
                }
            }
        }
    }
}