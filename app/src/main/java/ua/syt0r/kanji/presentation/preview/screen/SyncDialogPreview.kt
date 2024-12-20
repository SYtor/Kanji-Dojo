package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.sync.SyncDataDiffType
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.screen.main.SyncDialog
import ua.syt0r.kanji.presentation.screen.main.SyncDialogState

@Preview
@Composable
private fun BasePreview(
    state: SyncDialogState = SyncDialogState.Uploading
) {
    AppTheme(false) {
        SyncDialog(
            state = rememberUpdatedState(state),
            cancelSync = { },
            resolveConflict = {},
            navigateToAccount = {}
        )
    }
}

@Preview
@Composable
private fun DownloadPreview() = BasePreview(
    state = SyncDialogState.Downloading
)

@Preview
@Composable
private fun ErrorPreview(
    issue: ApiRequestIssue = ApiRequestIssue.NoSubscription
) = BasePreview(
    state = SyncDialogState.Error.Api(
        showDialog = remember { mutableStateOf(true) },
        issue = issue
    )
)

@Preview
@Composable
private fun AccountErrorPreview() = BasePreview(
    state = SyncDialogState.Error.Api(
        showDialog = remember { mutableStateOf(true) },
        issue = ApiRequestIssue.NotAuthenticated
    )
)

@Preview
@Composable
private fun OtherErrorPreview() = BasePreview(
    state = SyncDialogState.Error.Api(
        showDialog = remember { mutableStateOf(true) },
        issue = ApiRequestIssue.Other(Throwable("Other issue occured"))
    )
)



@Preview
@Composable
private fun ConflictRemotePreview() = BasePreview(
    state = SyncDialogState.Conflict(
        diffType = SyncDataDiffType.RemoteNewer,
        remoteDataTime = null,
        lastSyncTime = null
    )
)

@Preview
@Composable
private fun ConflictLocalPreview() = BasePreview(
    state = SyncDialogState.Conflict(
        diffType = SyncDataDiffType.Incompatible,
        remoteDataTime = null,
        lastSyncTime = null
    )
)

