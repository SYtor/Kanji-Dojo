package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch

@Composable
actual fun rememberPracticeSharer(snackbarHostState: SnackbarHostState): PracticeSharer {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    return remember {
        object : PracticeSharer {
            override fun share(data: String) {
                clipboardManager.setText(AnnotatedString(data))
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Practice characters were copied to the clipboard",
                        withDismissAction = true
                    )
                }
            }
        }
    }
}