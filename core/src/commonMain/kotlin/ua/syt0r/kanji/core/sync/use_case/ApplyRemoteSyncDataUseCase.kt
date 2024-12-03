package ua.syt0r.kanji.core.sync.use_case

import io.ktor.utils.io.jvm.javaio.toInputStream
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.backup.BackupManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.SyncBackupFileManager
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.transferToCompat
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import java.io.DataInputStream

interface ApplyRemoteSyncDataUseCase {
    suspend operator fun invoke(): SyncState
}

class DefaultApplyRemoteSyncDataUseCase(
    private val syncBackupFileManager: SyncBackupFileManager,
    private val backupManager: BackupManager,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val networkApi: NetworkApi
) : ApplyRemoteSyncDataUseCase {

    override suspend fun invoke(): SyncState {
        Logger.logMethod()

        kotlin.runCatching {
            val byteReadChannel = networkApi.getSyncData().getOrThrow()

            val inputStream = byteReadChannel.toInputStream()
            val dataInputStream = DataInputStream(inputStream)

            val infoLength = dataInputStream.readInt()
            val infoJson = dataInputStream.readUTF()
            Logger.d("infoLength[$infoLength] infoJson[$infoJson]")

            inputStream.transferToCompat(syncBackupFileManager.outputStream())

            backupManager.restore(syncBackupFileManager.getFile())

            appPreferences.lastSyncedDataInfoJson.set(infoJson)

            syncBackupFileManager.clean()

            return SyncState.Enabled.UpToDate
        }.getOrElse {
            syncBackupFileManager.clean()
            return SyncState.Error.Fail(it)
        }

    }

}