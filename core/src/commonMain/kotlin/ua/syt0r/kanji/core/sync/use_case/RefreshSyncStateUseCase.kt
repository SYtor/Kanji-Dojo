package ua.syt0r.kanji.core.sync.use_case

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.CurrentSyncDataVersion
import ua.syt0r.kanji.core.sync.HttpResponseException
import ua.syt0r.kanji.core.sync.SyncDataInfo
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract


interface RefreshSyncStateUseCase {
    suspend operator fun invoke(): SyncState
}

class DefaultRefreshSyncStateUseCase(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val getLocalSyncDataInfoUseCase: GetLocalSyncDataInfoUseCase,
    private val httpClient: HttpClient,
    private val json: Json
) : RefreshSyncStateUseCase {

    override suspend fun invoke(): SyncState {
        Logger.logMethod()
        val isSyncEnabled = appPreferences.syncEnabled.get()
        if (!isSyncEnabled) return SyncState.Disabled

        val remoteSyncDataInfo = kotlin.runCatching {
            val response = httpClient.get(NetworkApi.Url.GET_BACKUP_INFO)

            when (response.status) {
                HttpStatusCode.OK -> {
                    val jsonValue = response.bodyAsText()
                    json.decodeFromString<SyncDataInfo>(jsonValue)
                }

                HttpStatusCode.NoContent -> return SyncState.Enabled.PendingUpload
                else -> throw HttpResponseException(response.status)
            }
        }.getOrElse { exception ->
            return when {
                exception is HttpResponseException && exception.statusCode == HttpStatusCode.Unauthorized -> {
                    SyncState.Error.AuthExpired
                }

                else -> SyncState.Error.Fail(exception)
            }
        }

        val cachedRemoteSyncDataInfo = appPreferences.lastSyncedDataInfoJson.get()?.let {
            json.decodeFromString<SyncDataInfo>(it)
        }

        val localSyncDataInfo = getLocalSyncDataInfoUseCase()

        val isLocalDataOutdated = cachedRemoteSyncDataInfo != null
                && remoteSyncDataInfo != cachedRemoteSyncDataInfo
        val isRemoteDataSupported = remoteSyncDataInfo.dataVersion <= CurrentSyncDataVersion

        return when {
            localSyncDataInfo == remoteSyncDataInfo -> SyncState.Enabled.UpToDate

            isLocalDataOutdated || !isRemoteDataSupported -> {
                SyncState.Conflict(
                    remoteDataInfo = remoteSyncDataInfo,
                    localDataInfo = localSyncDataInfo,
                    cachedDataInfo = cachedRemoteSyncDataInfo
                )
            }

            else -> {
                SyncState.Enabled.PendingUpload
            }
        }
    }

}