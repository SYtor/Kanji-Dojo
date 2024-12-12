package ua.syt0r.kanji.core.sync.use_case

import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.backup.BackupManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.SyncBackupFileManager
import ua.syt0r.kanji.core.toApiType
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface UploadSyncDataUseCase {
    suspend operator fun invoke(): UploadSyncDataResult
}

sealed interface UploadSyncDataResult {
    object Success : UploadSyncDataResult
    data class Fail(val issue: ApiRequestIssue) : UploadSyncDataResult
}

class DefaultUploadSyncDataUseCase(
    private val getLocalSyncDataInfoUseCase: GetLocalSyncDataInfoUseCase,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val networkApi: NetworkApi,
    private val syncBackupFileManager: SyncBackupFileManager,
    private val backupManager: BackupManager,
    private val json: Json
) : UploadSyncDataUseCase {

    override suspend fun invoke(): UploadSyncDataResult = kotlin.runCatching {
        Logger.logMethod()

        val localSyncDataInfo = getLocalSyncDataInfoUseCase()

        val backupFile = syncBackupFileManager.getFile()
        backupManager.performBackup(backupFile)

        networkApi.updateSyncData(
            info = localSyncDataInfo.toApiType(),
            file = syncBackupFileManager.getChannelProvider()
        ).getOrThrow()

        syncBackupFileManager.clean()

        appPreferences.lastSyncedDataInfo.set(localSyncDataInfo)

        UploadSyncDataResult.Success
    }.getOrElse {
        syncBackupFileManager.clean()
        UploadSyncDataResult.Fail(issue = ApiRequestIssue.classify(it))
    }

}
