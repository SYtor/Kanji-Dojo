package ua.syt0r.kanji.core.sync.use_case

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.HttpResponseException
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.CurrentSyncDataVersion
import ua.syt0r.kanji.core.sync.SyncDataDiffType
import ua.syt0r.kanji.core.sync.SyncDataInfo
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract


interface RefreshSyncStateUseCase {
    suspend operator fun invoke(): SyncStateRefreshResult
}


sealed interface SyncStateRefreshResult {

    data class NoRemoteData(
        val localDataInfo: SyncDataInfo
    ) : SyncStateRefreshResult

    data class WithRemoteData(
        val diffType: SyncDataDiffType,
        val localDataInfo: SyncDataInfo,
        val cachedDataInfo: SyncDataInfo?,
        val remoteDataInfo: SyncDataInfo
    ) : SyncStateRefreshResult

    data class Error(
        val issue: ApiRequestIssue
    ) : SyncStateRefreshResult

}


class DefaultRefreshSyncStateUseCase(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val getLocalSyncDataInfoUseCase: GetLocalSyncDataInfoUseCase,
    private val networkApi: NetworkApi,
    private val json: Json
) : RefreshSyncStateUseCase {

    override suspend fun invoke(): SyncStateRefreshResult {
        Logger.logMethod()

        val localSyncDataInfo = getLocalSyncDataInfoUseCase()

        val remoteApiSyncDataInfo = networkApi.getSyncDataInfo().getOrElse {
            if (it is HttpResponseException && it.statusCode == HttpStatusCode.NoContent) {
                return SyncStateRefreshResult.NoRemoteData(localSyncDataInfo)
            }
            return SyncStateRefreshResult.Error(ApiRequestIssue.classify(it))
        }

        val remoteSyncDataInfo = SyncDataInfo(
            dataId = remoteApiSyncDataInfo.dataId,
            dataVersion = remoteApiSyncDataInfo.dataVersion,
            dataTimestamp = remoteApiSyncDataInfo.dataTimestamp
        )

        val cachedRemoteSyncDataInfo = appPreferences.lastSyncedDataInfoJson.get()?.let {
            json.decodeFromString<SyncDataInfo>(it)
        }

        val isRemoteDataSupported = remoteSyncDataInfo.dataVersion <= CurrentSyncDataVersion
        val isRemoteDataChangedSinceLastSync = cachedRemoteSyncDataInfo
            ?.equals(remoteSyncDataInfo) == false

        val isRemoteDataNewer = run {
            val remoteTimestamp = remoteSyncDataInfo.dataTimestamp ?: return@run false
            val localTimestamp = localSyncDataInfo.dataTimestamp ?: return@run true
            remoteTimestamp > localTimestamp
        }

        val diffType = when {
            remoteSyncDataInfo == localSyncDataInfo -> SyncDataDiffType.Equal
            !isRemoteDataSupported -> SyncDataDiffType.RemoteUnsupported
            isRemoteDataChangedSinceLastSync -> SyncDataDiffType.Incompatible
            isRemoteDataNewer -> SyncDataDiffType.RemoteNewer
            else -> SyncDataDiffType.LocalNewer
        }

        return SyncStateRefreshResult.WithRemoteData(
            diffType = diffType,
            localDataInfo = localSyncDataInfo,
            cachedDataInfo = cachedRemoteSyncDataInfo,
            remoteDataInfo = remoteSyncDataInfo,
        )
    }

}