package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.sync.SyncConflictResolveStrategy
import ua.syt0r.kanji.core.sync.SyncDataDiffType
import ua.syt0r.kanji.presentation.common.ExperimentalMultiplatformDialog


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SyncDialog(
    state: State<SyncDialogState>,
    cancelSync: () -> Unit,
    resolveConflict: (SyncConflictResolveStrategy) -> Unit,
    navigateToAccount: () -> Unit
) {

    val dialogContent: @Composable ColumnScope.() -> Unit
    val dialogButtons: @Composable FlowRowScope.() -> Unit

    when (val currentState = state.value) {
        SyncDialogState.Hidden -> return

        SyncDialogState.Uploading -> {
            dialogContent = {
                LoadingLayout(
                    imageVector = Icons.Outlined.CloudUpload,
                    message = "Uploading..."
                )
            }
            dialogButtons = {
                DialogButton(cancelSync) { Text("Cancel") }
            }
        }

        SyncDialogState.Downloading -> {
            dialogContent = {
                LoadingLayout(
                    imageVector = Icons.Outlined.CloudDownload,
                    message = "Downloading..."
                )
            }
            dialogButtons = {
                DialogButton(cancelSync) { Text("Cancel") }
            }
        }

        is SyncDialogState.Conflict -> {
            dialogContent = {
                val (title, message) = when (currentState.diffType) {
                    SyncDataDiffType.RemoteNewer -> "New Data Found" to "Data on the server is newer than your local copy"
                    SyncDataDiffType.Incompatible -> "Data Conflict" to "Both remote and local data were changed since the last sync, result can't be merged"
                    else -> error("Unexpected diff type for conflict state")
                }
                MessageLayout(
                    title = title,
                    message = message,
                    imageVector = Icons.Outlined.CloudSync
                )
            }
            dialogButtons = {

                if (currentState.diffType == SyncDataDiffType.Incompatible) {
                    DialogButton(
                        onClick = { resolveConflict(SyncConflictResolveStrategy.UploadLocal) }
                    ) {
                        DialogButtonIcon(Icons.Outlined.Upload)
                        Text("Upload")
                    }
                }

                if (
                    currentState.diffType in setOf(
                        SyncDataDiffType.RemoteNewer,
                        SyncDataDiffType.Incompatible
                    )
                ) {
                    DialogButton(
                        onClick = { resolveConflict(SyncConflictResolveStrategy.DownloadRemote) }
                    ) {
                        DialogButtonIcon(Icons.Outlined.Download)
                        Text("Download")
                    }
                }
                DialogButton(cancelSync) { Text("Cancel") }
            }
        }

        is SyncDialogState.Error.Api -> {
            if (!currentState.showDialog.value) return
            val hideErrorDialog = { currentState.showDialog.value = false }
            dialogContent = {
                val (title, message) = when (val issue = currentState.issue) {
                    ApiRequestIssue.NoConnection -> "No Network" to "Couldn't establish network connection"
                    ApiRequestIssue.NoSubscription -> "Subscription Expired" to "Your subscription has expired, sync will be disabled"
                    ApiRequestIssue.NotAuthenticated -> "Session Expired" to "Sign in to your account again to continue"
                    is ApiRequestIssue.Other -> "Unexpected Error" to
                            (issue.throwable.message ?: "Unknown issue")
                }
                MessageLayout(
                    title = title,
                    message = message,
                    imageVector = Icons.Outlined.SyncProblem,
                    titleColor = MaterialTheme.colorScheme.error
                )
            }
            dialogButtons = {
                when (currentState.issue) {
                    ApiRequestIssue.NoSubscription,
                    ApiRequestIssue.NotAuthenticated -> {
                        DialogButton(navigateToAccount) { Text("Account") }
                    }

                    else -> Unit
                }
                DialogButton(hideErrorDialog) { Text("Cancel") }
            }
        }

        is SyncDialogState.Error.Unsupported -> {
            if (!currentState.showDialog.value) return
            val hideErrorDialog = { currentState.showDialog.value = false }
            dialogContent = {
                MessageLayout(
                    title = "Data on the server is unsupported",
                    message = "The data on the server was created using the newer version of the application and is not compatible with the currently installed version. Update the app to retrieve your data or upload your local data to the server",
                )
            }
            dialogButtons = {
                DialogButton(
                    onClick = { resolveConflict(SyncConflictResolveStrategy.UploadLocal) }
                ) { Text("Upload") }
                DialogButton(hideErrorDialog) { Text("Cancel") }
            }
        }
    }

    ExperimentalMultiplatformDialog(
        onDismissRequest = {},
        title = { Text(text = "Sync") },
        content = dialogContent,
        buttons = dialogButtons
    )

}

@Composable
private fun DialogButtonIcon(imageVector: ImageVector) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier.size(20.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.DialogButton(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.labelLarge,
        LocalContentColor provides MaterialTheme.colorScheme.surface,
        LocalRippleConfiguration provides RippleConfiguration(
            MaterialTheme.colorScheme.surface.copy(0.27f)
        )
    ) {

        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.onSurface)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .fillMaxRowHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )

    }
}

@Composable
private fun LoadingLayout(
    imageVector: ImageVector,
    message: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        androidx.compose.material.LinearProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Text(message)
    }
}

@Composable
private fun MessageLayout(
    title: String,
    message: String,
    imageVector: ImageVector = Icons.Outlined.CloudSync,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {

        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(60.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = titleColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}