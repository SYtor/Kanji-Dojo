package ua.syt0r.kanji.core.sync.use_case

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.HttpResponseException
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.CurrentSyncDataVersion
import ua.syt0r.kanji.core.sync.SyncDataInfo
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract


interface RefreshSyncStateUseCase {
    suspend operator fun invoke(): SyncState
}

class DefaultRefreshSyncStateUseCase(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val getLocalSyncDataInfoUseCase: GetLocalSyncDataInfoUseCase,
    private val networkApi: NetworkApi,
    private val json: Json
) : RefreshSyncStateUseCase {

    override suspend fun invoke(): SyncState {
        Logger.logMethod()
        val isSyncEnabled = appPreferences.syncEnabled.get()
        if (!isSyncEnabled) return SyncState.Disabled

        val remoteSyncDataInfo: SyncDataInfo = networkApi.getSyncDataInfo().getOrElse { exception ->
            return if (exception is HttpResponseException) when (exception.statusCode) {
                HttpStatusCode.Unauthorized -> SyncState.Error.AuthExpired
                HttpStatusCode.NoContent -> SyncState.Enabled.PendingUpload
                else -> SyncState.Error.Fail(exception)
            } else {
                SyncState.Error.Fail(exception)
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