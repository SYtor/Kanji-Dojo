package ua.syt0r.kanji.presentation.screen.main.screen.deck_details

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

interface PracticeSharer {
    fun share(data: String)
}

@Composable
expect fun rememberPracticeSharer(snackbarHostState: SnackbarHostState): PracticeSharer