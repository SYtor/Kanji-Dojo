package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.byUnicodePattern
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.sync.SyncConflictResolveStrategy
import ua.syt0r.kanji.core.sync.SyncDataDiffType
import ua.syt0r.kanji.presentation.common.MultiplatformDialog


@Composable
fun SyncDialog(
    state: State<SyncDialogState>,
    cancelSync: () -> Unit,
    resolveConflict: (SyncConflictResolveStrategy) -> Unit
) {

    val dialogContent: @Composable ColumnScope.() -> Unit
    val dialogButtons: @Composable RowScope.() -> Unit

    when (val currentState = state.value) {
        SyncDialogState.Hidden -> return

        SyncDialogState.Uploading -> {
            dialogContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Text("Uploading...")
                }
            }
            dialogButtons = {
                TextButton(
                    onClick = cancelSync
                ) { Text("Cancel") }
            }
        }

        SyncDialogState.Downloading -> {
            dialogContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Text("Downloading...")
                }
            }
            dialogButtons = {
                TextButton(
                    onClick = cancelSync
                ) { Text("Cancel") }
            }
        }

        is SyncDialogState.Conflict -> {
            dialogContent = {
                val localDateTimeFormat = LocalDateTime.Format {
                    byUnicodePattern("yyyy-MM-dd HH:mm")
                }

                val remoteTime = currentState.remoteDataTime
                    ?.format(localDateTimeFormat) ?: "-"
                val localTime = currentState.lastSyncTime
                    ?.format(localDateTimeFormat) ?: "-"

                val (title, message) = when (currentState.diffType) {
                    SyncDataDiffType.RemoteNewer -> "New Data Found" to "Data on the server is newer than your local copy"
                    SyncDataDiffType.Incompatible -> "Data Conflict" to "Both remote and local data were changed since the last sync, result can't be merged"
                    else -> error("Unexpected diff type for conflict state")
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(message)
                }

            }
            dialogButtons = {
                TextButton(
                    onClick = cancelSync
                ) { Text("Cancel") }

                if (currentState.diffType == SyncDataDiffType.Incompatible) {
                    TextButton(
                        onClick = { resolveConflict(SyncConflictResolveStrategy.UploadLocal) }
                    ) { Text("Upload") }
                }

                if (
                    currentState.diffType in setOf(
                        SyncDataDiffType.RemoteNewer,
                        SyncDataDiffType.Incompatible
                    )
                ) {
                    TextButton(
                        onClick = { resolveConflict(SyncConflictResolveStrategy.DownloadRemote) }
                    ) { Text("Download") }
                }
            }
        }

        is SyncDialogState.Error.Api -> {
            if (!currentState.showDialog.value) return
            dialogContent = {
                val (title, message) = when (val issue = currentState.issue) {
                    ApiRequestIssue.NoConnection -> "No Network" to "Couldn't establish network connection"
                    ApiRequestIssue.NoSubscription -> "Subscription Expired" to "Your subscription has expired, sync will be disabled"
                    ApiRequestIssue.NotAuthenticated -> "Account Issue" to "There's an issue with your account"
                    is ApiRequestIssue.Other -> "Unexpected Error" to issue.throwable.message!!
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            dialogButtons = {
                TextButton(
                    onClick = { currentState.showDialog.value = false }
                ) { Text("Cancel") }
            }
        }

        is SyncDialogState.Error.Unsupported -> {
            if (!currentState.showDialog.value) return
            dialogContent = {
                Column {
                    Text("Data on the server is unsupported")
                    Text("The data on the server was created using the newer version of the application and can't be applied to the currently installed version. Update the app to retrieve your data or upload your local data to the server")
                }

            }
            dialogButtons = {
                TextButton(
                    onClick = { currentState.showDialog.value = false }
                ) { Text("Cancel") }

                TextButton(
                    onClick = { resolveConflict(SyncConflictResolveStrategy.UploadLocal) }
                ) { Text("Upload") }
            }
        }
    }

    MultiplatformDialog(
        onDismissRequest = {},
        title = { Text(text = "Sync") },
        content = dialogContent,
        buttons = dialogButtons
    )

}