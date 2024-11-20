package ua.syt0r.kanji.core.sync.use_case

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
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
    private val httpClient: HttpClient,
    private val syncBackupFileManager: SyncBackupFileManager,
    private val backupManager: BackupManager,
    private val appPreferences: PreferencesContract.AppPreferences
) : ApplyRemoteSyncDataUseCase {

    override suspend fun invoke(): SyncState {
        Logger.logMethod()

        kotlin.runCatching {
            val response = httpClient.get(NetworkApi.Url.GET_BACKUP)

            val inputStream = response.bodyAsChannel().toInputStream()
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