package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.screen.main.SyncDialog
import ua.syt0r.kanji.presentation.screen.main.SyncDialogState

@Preview
@Composable
private fun BasePreview(
    state: SyncDialogState = SyncDialogState.Uploading
) {
    AppTheme {
        SyncDialog(
            state = rememberUpdatedState(state),
            cancelSync = { },
            resolveConflict = {}
        )
    }
}

@Preview
@Composable
private fun ApiErrorPreview() = BasePreview(
    state = SyncDialogState.Error.Api(
        showDialog = remember { mutableStateOf(true) },
        issue = ApiRequestIssue.NoSubscription
    )
)