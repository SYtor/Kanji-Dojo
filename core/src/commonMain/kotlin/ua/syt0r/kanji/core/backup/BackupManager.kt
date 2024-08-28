package ua.syt0r.kanji.core.backup

import kotlinx.coroutines.flow.SharedFlow
import java.io.InputStream
import java.io.OutputStream

interface BackupManager {
    suspend fun performBackup(location: PlatformFile)
    suspend fun readBackupInfo(location: PlatformFile): BackupInfo
    suspend fun restore(location: PlatformFile)
}

interface BackupRestoreCompletionNotifier {
    suspend fun notify()
}

interface BackupRestoreEventsProvider {
    val onRestoreEventsFlow: SharedFlow<Unit>
}

// TODO make `expect` class when out of beta
interface PlatformFile

interface PlatformFileHandler {
    fun getInputStream(file: PlatformFile): InputStream
    fun getOutputStream(file: PlatformFile): OutputStream
}
