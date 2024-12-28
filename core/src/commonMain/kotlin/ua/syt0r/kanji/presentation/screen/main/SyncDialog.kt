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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ua.syt0r.kanji.Res
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.sync.SyncConflictResolveStrategy
import ua.syt0r.kanji.core.sync.SyncDataDiffType
import ua.syt0r.kanji.presentation.common.ExperimentalMultiplatformDialog
import ua.syt0r.kanji.sync_dialog_api_no_network_message
import ua.syt0r.kanji.sync_dialog_api_no_network_title
import ua.syt0r.kanji.sync_dialog_button_account
import ua.syt0r.kanji.sync_dialog_button_cancel
import ua.syt0r.kanji.sync_dialog_button_download
import ua.syt0r.kanji.sync_dialog_button_upload
import ua.syt0r.kanji.sync_dialog_conflict_incompatible_message
import ua.syt0r.kanji.sync_dialog_conflict_incompatible_title
import ua.syt0r.kanji.sync_dialog_conflict_remote_newer_message
import ua.syt0r.kanji.sync_dialog_conflict_remote_newer_title
import ua.syt0r.kanji.sync_dialog_downloading_message
import ua.syt0r.kanji.sync_dialog_title
import ua.syt0r.kanji.sync_dialog_unsupported_error_message
import ua.syt0r.kanji.sync_dialog_unsupported_error_title
import ua.syt0r.kanji.sync_dialog_uploading_message


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
                    message = stringResource(Res.string.sync_dialog_uploading_message)
                )
            }
            dialogButtons = {
                DialogButton(cancelSync) {
                    Text(stringResource(Res.string.sync_dialog_button_cancel))
                }
            }
        }

        SyncDialogState.Downloading -> {
            dialogContent = {
                LoadingLayout(
                    imageVector = Icons.Outlined.CloudDownload,
                    message = stringResource(Res.string.sync_dialog_downloading_message)
                )
            }
            dialogButtons = {
                DialogButton(cancelSync) {
                    Text(stringResource(Res.string.sync_dialog_button_cancel))
                }
            }
        }

        is SyncDialogState.Conflict -> {
            dialogContent = {
                val title: StringResource
                val message: StringResource

                when (currentState.diffType) {
                    SyncDataDiffType.RemoteNewer -> {
                        title = Res.string.sync_dialog_conflict_remote_newer_title
                        message = Res.string.sync_dialog_conflict_remote_newer_message
                    }

                    SyncDataDiffType.Incompatible -> {
                        title = Res.string.sync_dialog_conflict_incompatible_title
                        message = Res.string.sync_dialog_conflict_incompatible_message
                    }

                    else -> error("Unexpected diffType[${currentState.diffType}] for conflict state")
                }

                MessageLayout(
                    title = stringResource(title),
                    message = stringResource(message),
                    imageVector = Icons.Outlined.CloudSync
                )
            }
            dialogButtons = {

                if (currentState.diffType == SyncDataDiffType.Incompatible) {
                    DialogButton(
                        onClick = { resolveConflict(SyncConflictResolveStrategy.UploadLocal) }
                    ) {
                        DialogButtonIcon(Icons.Outlined.Upload)
                        Text(stringResource(Res.string.sync_dialog_button_upload))
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
                        Text(stringResource(Res.string.sync_dialog_button_download))
                    }
                }
                DialogButton(cancelSync) {
                    Text(stringResource(Res.string.sync_dialog_button_cancel))
                }
            }
        }

        is SyncDialogState.Error.Api -> {
            if (!currentState.showDialog.value) return
            val hideErrorDialog = { currentState.showDialog.value = false }
            dialogContent = {
                val title: String
                val message: String

                when (val issue = currentState.issue) {
                    ApiRequestIssue.NoConnection -> {
                        title = stringResource(Res.string.sync_dialog_api_no_network_title)
                        message = stringResource(Res.string.sync_dialog_api_no_network_message)
                    }

                    ApiRequestIssue.NoSubscription -> {
                        title = stringResource(Res.string.sync_dialog_api_no_network_title)
                        message = stringResource(Res.string.sync_dialog_api_no_network_message)
                    }

                    ApiRequestIssue.NotAuthenticated -> {
                        title = stringResource(Res.string.sync_dialog_api_no_network_title)
                        message = stringResource(Res.string.sync_dialog_api_no_network_message)
                    }

                    is ApiRequestIssue.Other -> {
                        title = stringResource(Res.string.sync_dialog_api_no_network_title)
                        message = issue.throwable.message
                            ?: stringResource(Res.string.sync_dialog_api_no_network_message)
                    }
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
                        DialogButton(navigateToAccount) {
                            Text(stringResource(Res.string.sync_dialog_button_account))
                        }
                    }

                    else -> Unit
                }
                DialogButton(hideErrorDialog) {
                    Text(stringResource(Res.string.sync_dialog_button_cancel))
                }
            }
        }

        is SyncDialogState.Error.Unsupported -> {
            if (!currentState.showDialog.value) return
            val hideErrorDialog = { currentState.showDialog.value = false }
            dialogContent = {
                MessageLayout(
                    title = stringResource(Res.string.sync_dialog_unsupported_error_title),
                    message = stringResource(Res.string.sync_dialog_unsupported_error_message),
                )
            }
            dialogButtons = {
                DialogButton(
                    onClick = { resolveConflict(SyncConflictResolveStrategy.UploadLocal) }
                ) { Text(stringResource(Res.string.sync_dialog_button_upload)) }
                DialogButton(hideErrorDialog) {
                    Text(stringResource(Res.string.sync_dialog_button_cancel))
                }
            }
        }
    }

    ExperimentalMultiplatformDialog(
        onDismissRequest = {},
        title = { Text(stringResource(Res.string.sync_dialog_title)) },
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