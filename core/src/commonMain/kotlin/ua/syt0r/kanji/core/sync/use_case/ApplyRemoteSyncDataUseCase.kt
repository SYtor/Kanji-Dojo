package ua.syt0r.kanji.core.sync.use_case

import io.ktor.utils.io.jvm.javaio.toInputStream
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.ApiSyncDataInfo
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.backup.BackupManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.SyncBackupFileManager
import ua.syt0r.kanji.core.toPreferencesType
import ua.syt0r.kanji.core.transferToCompat
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.common.json
import java.io.DataInputStream

interface ApplyRemoteSyncDataUseCase {
    suspend operator fun invoke(): ApplySyncResult
}

sealed interface ApplySyncResult {
    object Success : ApplySyncResult
    data class Fail(val issue: ApiRequestIssue) : ApplySyncResult
}

class DefaultApplyRemoteSyncDataUseCase(
    private val syncBackupFileManager: SyncBackupFileManager,
    private val backupManager: BackupManager,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val networkApi: NetworkApi
) : ApplyRemoteSyncDataUseCase {

    override suspend fun invoke(): ApplySyncResult {
        Logger.logMethod()

        val result = runCatching {
            val byteReadChannel = networkApi.getSyncData().getOrThrow()

            val inputStream = byteReadChannel.toInputStream()
            val dataInputStream = DataInputStream(inputStream)

            val infoLength = dataInputStream.readInt()
            val infoJson = dataInputStream.readUTF()
            Logger.d("infoLength[$infoLength] infoJson[$infoJson]")

            val syncDataInfo = json.decodeFromString<ApiSyncDataInfo>(infoJson)
                .toPreferencesType()

            inputStream.transferToCompat(syncBackupFileManager.outputStream())

            backupManager.restore(syncBackupFileManager.getFile())

            appPreferences.lastSyncedDataInfo.set(syncDataInfo)

            ApplySyncResult.Success
        }.getOrElse {
            ApplySyncResult.Fail(ApiRequestIssue.classify(it))
        }

        syncBackupFileManager.clean()

        return result
    }

}