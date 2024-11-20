package ua.syt0r.kanji.core.sync.use_case

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.backup.BackupManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.HttpResponseException
import ua.syt0r.kanji.core.sync.SyncBackupFileManager
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface UploadSyncDataUseCase {
    suspend operator fun invoke(): SyncState
}

class DefaultUploadSyncDataUseCase(
    private val getLocalSyncDataInfoUseCase: GetLocalSyncDataInfoUseCase,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val httpClient: HttpClient,
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

        val response = httpClient.post(NetworkApi.Url.UPDATE_BACKUP) {
            val partDataList = formData {
                append("info", infoJson)
                append("data", syncBackupFileManager.getChannelProvider(), Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"data.zip\"")
                })
            }
            setBody(MultiPartFormDataContent(partDataList))
        }

        syncBackupFileManager.clean()

        when (response.status) {
            HttpStatusCode.OK -> {
                appPreferences.lastSyncedDataInfoJson.set(infoJson)
                SyncState.Enabled.UpToDate
            }

            HttpStatusCode.Unauthorized -> {
                SyncState.Error.AuthExpired
            }

            HttpStatusCode.PaymentRequired -> {
                SyncState.Error.MissingSubscription
            }

            else -> throw HttpResponseException(response.status)
        }
    }.getOrElse {
        syncBackupFileManager.clean()
        SyncState.Error.Fail(it)
    }

}
