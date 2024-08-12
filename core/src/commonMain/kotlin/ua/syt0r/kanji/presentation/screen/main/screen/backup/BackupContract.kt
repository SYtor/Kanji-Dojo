package ua.syt0r.kanji.presentation.screen.main.screen.backup

import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.backup.PlatformFile

interface BackupContract {

    interface ViewModel {

        val state: StateFlow<ScreenState>

        fun createBackup(file: PlatformFile)
        fun readBackupInfo(file: PlatformFile)
        fun restoreFromBackup()

    }

    sealed interface ScreenState {

        object Idle : ScreenState
        object Loading : ScreenState
        object UninterruptibleLoading : ScreenState

        data class Error(
            val message: String?
        ) : ScreenState

        data class RestoreConfirmation(
            val file: PlatformFile,
            val currentDbVersion: Long,
            val backupDbVersion: Long,
            val backupCreateInstant: Instant
        ) : ScreenState

        object ActionCompleted : ScreenState

    }

}