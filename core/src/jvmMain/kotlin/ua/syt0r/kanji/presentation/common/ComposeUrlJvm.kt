package ua.syt0r.kanji.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberUrlHandler(): UrlHandler {
    return remember {
        object : UrlHandler {
            override fun openInBrowser(url: String) {
                TODO("Not yet implemented")
            }
        }
    }
}