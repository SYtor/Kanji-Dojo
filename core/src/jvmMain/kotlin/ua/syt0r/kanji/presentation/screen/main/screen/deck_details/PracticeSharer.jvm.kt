package ua.syt0r.kanji.presentation.screen.main.screen.deck_details

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch
import ua.syt0r.kanji.presentation.common.resources.string.resolveString

@Composable
actual fun rememberPracticeSharer(snackbarHostState: SnackbarHostState): PracticeSharer {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val copyMessage = resolveString { deckDetails.shareLetterDeckClipboardMessage }
    return remember {
        object : PracticeSharer {
            override fun share(data: String) {
                clipboardManager.setText(AnnotatedString(data))
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = copyMessage,
                        withDismissAction = true
                    )
                }
            }
        }
    }
}