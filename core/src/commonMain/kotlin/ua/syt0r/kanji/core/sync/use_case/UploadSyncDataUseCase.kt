package ua.syt0r.kanji.core.sync.use_case

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.HttpResponseException
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.backup.BackupManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.SyncBackupFileManager
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface UploadSyncDataUseCase {
    suspend operator fun invoke(): SyncState
}

class DefaultUploadSyncDataUseCase(
    private val getLocalSyncDataInfoUseCase: GetLocalSyncDataInfoUseCase,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val networkApi: NetworkApi,
    private val syncBackupFileManager: SyncBackupFileManager,
    private val backupManager: BackupManager,
    private val json: Json
) : UploadSyncDataUseCase {

    override suspend fun invoke(): SyncState = kotlin.runCatching {
        Logger.logMethod()

        val localSyncDataInfo = getLocalSyncDataInfoUseCase()
        val infoJson = json.encodeToString(localSyncDataInfo)

        val backupFile = syncBackupFileManager.getFile()
        backupManager.performBackup(backupFile)

        networkApi.updateSyncData(
            info = localSyncDataInfo,
            file = syncBackupFileManager.getChannelProvider()
        ).getOrThrow()

        syncBackupFileManager.clean()

        appPreferences.lastSyncedDataInfoJson.set(infoJson)
        SyncState.Enabled.UpToDate
    }.getOrElse {
        syncBackupFileManager.clean()

        if (it is HttpResponseException) {
            when (it.statusCode) {
                HttpStatusCode.Unauthorized -> {
                    SyncState.Error.AuthExpired
                }

                HttpStatusCode.PaymentRequired -> {
                    SyncState.Error.MissingSubscription
                }

                else -> SyncState.Error.Fail(it)
            }
        } else {
            SyncState.Error.Fail(it)
        }

    }

}
