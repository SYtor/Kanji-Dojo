package ua.syt0r.kanji.presentation.screen.main.screen.backup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
fun BackupScreen(
    mainNavigationState: MainNavigationState,
    viewModel: BackupContract.ViewModel = getMultiplatformViewModel()
) {

    BackupScreenUI(
        state = viewModel.state.collectAsState(),
        onUpButtonClick = { mainNavigationState.navigateBack() },
        createBackup = { viewModel.createBackup(it) },
        readBackup = { viewModel.readBackupInfo(it) },
        restoreFromBackup = { viewModel.restoreFromBackup() }
    )

}