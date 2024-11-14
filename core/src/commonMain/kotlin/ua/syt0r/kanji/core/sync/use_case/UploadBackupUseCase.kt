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
import ua.syt0r.kanji.core.sync.ApiBackupInfo
import ua.syt0r.kanji.core.sync.HttpResponseException
import ua.syt0r.kanji.core.sync.SyncBackupFileManager
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.user_data.db.UserDataDatabase
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface UploadBackupUseCase {
    suspend operator fun invoke(): SyncState
}

class DefaultUploadBackupUseCase(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val httpClient: HttpClient,
    private val syncFileManager: SyncBackupFileManager,
    private val backupManager: BackupManager,
    private val json: Json
) : UploadBackupUseCase {

    override suspend fun invoke(): SyncState = kotlin.runCatching {
        val localApiBackupInfo = ApiBackupInfo(
            dataId = appPreferences.localDataId.get(),
            dataVersion = UserDataDatabase.Schema.version,
            dataTimestamp = appPreferences.localDataTimestamp.get()
        )
        val infoJson = json.encodeToString(localApiBackupInfo)

        val backupFile = syncFileManager.getFile()
        backupManager.performBackup(backupFile)

        val response = httpClient.post(NetworkApi.Url.UPDATE_BACKUP) {
            val partDataList = formData {
                append("info", infoJson)
                append("data", syncFileManager.getChannelProvider(), Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"data.zip\"")
                })
            }
            setBody(MultiPartFormDataContent(partDataList))
        }

        syncFileManager.clean()

        when (response.status) {
            HttpStatusCode.OK -> {
                appPreferences.lastSyncedDataInfoJson.set(infoJson)
                SyncState.NoChanges
            }

            HttpStatusCode.Unauthorized -> {
                SyncState.AuthExpired
            }

            HttpStatusCode.PaymentRequired -> {
                SyncState.MissingSubscription
            }

            else -> throw HttpResponseException(response.status)
        }
    }.getOrElse {
        syncFileManager.clean()
        SyncState.Fail(it)
    }

}
