package ua.syt0r.kanji.presentation.screen.main.screen.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.backup.PlatformFile
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.neutralButtonColors
import ua.syt0r.kanji.presentation.screen.main.screen.backup.BackupContract.ScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreenUI(
    state: State<ScreenState>,
    onUpButtonClick: () -> Unit,
    createBackup: (location: PlatformFile) -> Unit,
    readBackup: (location: PlatformFile) -> Unit,
    restoreFromBackup: () -> Unit
) {

    val strings = resolveString { backup }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.title) },
                navigationIcon = {
                    IconButton(onClick = onUpButtonClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier.padding(paddingValues)
                .fillMaxSize()
                .wrapContentWidth()
                .widthIn(max = 400.dp)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            val filePicker = rememberBackupFilePicker(
                onFileCreateCallback = {
                    if (it is FilePickResult.Picked) createBackup(it.file)
                },
                onFileSelectCallback = {
                    if (it is FilePickResult.Picked) readBackup(it.file)
                }
            )

            val currentState = state.value
            val buttonsEnabled = when (currentState) {
                ScreenState.Loading, ScreenState.UninterruptibleLoading -> false
                else -> true
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BackupButton(
                    onClick = { filePicker.startCreateFileFlow() },
                    enabled = buttonsEnabled,
                    icon = Icons.Default.SaveAlt,
                    text = strings.backupButton
                )
                BackupButton(
                    onClick = { filePicker.startSelectFileFlow() },
                    enabled = buttonsEnabled,
                    icon = Icons.Default.Restore,
                    text = strings.restoreButton
                )
            }

            when (currentState) {
                ScreenState.Idle -> {}
                ScreenState.Loading -> {
                    CircularProgressIndicator(
                        Modifier.align(Alignment.CenterHorizontally).padding(vertical = 20.dp)
                    )
                }

                is ScreenState.UninterruptibleLoading -> {
                    MultiplatformDialog(onDismissRequest = {}) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(300.dp).wrapContentSize()
                        )
                    }
                }

                is ScreenState.Error -> {
                    Text(
                        text = currentState.message ?: strings.unknownError,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is ScreenState.RestoreConfirmation -> {
                    Text(
                        text = strings.restoreVersionMessage(
                            currentState.backupDbVersion,
                            currentState.currentDbVersion
                        )
                    )
                    Text(
                        text = strings.restoreTimeMessage(currentState.backupCreateInstant)
                    )
                    Text(text = strings.restoreNote)
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min)
                    ) {
                        BackupButton(
                            onClick = restoreFromBackup,
                            enabled = true,
                            icon = Icons.Default.SettingsBackupRestore,
                            text = strings.restoreApplyButton
                        )
                    }
                }

                is ScreenState.ActionCompleted -> {
                    Text(
                        text = strings.completeMessage,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

        }

    }

}

@Composable
private fun RowScope.BackupButton(
    onClick: () -> Unit,
    enabled: Boolean,
    icon: ImageVector,
    text: String
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.weight(1f).fillMaxHeight(),
        colors = ButtonDefaults.neutralButtonColors(),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text = text, textAlign = TextAlign.Center)
    }
}
