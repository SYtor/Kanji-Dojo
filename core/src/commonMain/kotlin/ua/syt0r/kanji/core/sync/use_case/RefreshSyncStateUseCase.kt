package ua.syt0r.kanji.core.sync.use_case

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.sync.ApiBackupInfo
import ua.syt0r.kanji.core.sync.HttpResponseException
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.user_data.db.UserDataDatabase
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract


interface RefreshSyncStateUseCase {
    suspend operator fun invoke(): SyncState
}

class DefaultRefreshSyncStateUseCase(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val httpClient: HttpClient,
    private val json: Json
) : RefreshSyncStateUseCase {

    override suspend fun invoke(): SyncState {
        val isSyncEnabled = appPreferences.syncEnabled.get()
        if (!isSyncEnabled) return SyncState.Disabled

        val remoteBackupInfo = kotlin.runCatching {
            val response = httpClient.get(NetworkApi.Url.GET_BACKUP_INFO)

            when (response.status) {
                HttpStatusCode.OK -> {
                    val jsonValue = response.bodyAsText()
                    json.decodeFromString<ApiBackupInfo>(jsonValue)
                }

                HttpStatusCode.NoContent -> return SyncState.PendingUpload
                else -> throw HttpResponseException(response.status)
            }
        }.getOrElse { exception ->
            return when {
                exception is HttpResponseException &&
                        exception.statusCode == HttpStatusCode.Unauthorized -> SyncState.AuthExpired

                else -> SyncState.Fail(exception)
            }
        }

        val cachedRemoteBackupInfo = appPreferences.lastSyncedDataInfoJson.get()?.let {
            json.decodeFromString<ApiBackupInfo>(it)
        }

        val localBackupInfo = ApiBackupInfo(
            dataId = appPreferences.localDataId.get(),
            dataVersion = UserDataDatabase.Schema.version,
            dataTimestamp = appPreferences.localDataTimestamp.get(),
        )

        val isLocalDataOutdated = cachedRemoteBackupInfo != null &&
                remoteBackupInfo != cachedRemoteBackupInfo
        val isRemoteDataSupported = remoteBackupInfo.dataVersion <= UserDataDatabase.Schema.version

        return when {
            localBackupInfo == remoteBackupInfo -> SyncState.NoChanges

            isLocalDataOutdated || !isRemoteDataSupported -> {
                SyncState.Conflict(
                    remoteTime = remoteBackupInfo.dataTimestamp
                        ?.let { Instant.fromEpochMilliseconds(it) }
                )
            }

            else -> {
                SyncState.PendingUpload
            }
        }
    }

}