package ua.syt0r.kanji.core.sync.use_case

import io.ktor.http.HttpStatusCode
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.HttpResponseException
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.CurrentSyncDataVersion
import ua.syt0r.kanji.core.sync.SyncDataDiffType
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesSyncDataInfo


interface RefreshSyncStateUseCase {
    suspend operator fun invoke(): SyncStateRefreshResult
}


sealed interface SyncStateRefreshResult {

    data class NoRemoteData(
        val localDataInfo: PreferencesSyncDataInfo
    ) : SyncStateRefreshResult

    data class WithRemoteData(
        val diffType: SyncDataDiffType,
        val localDataInfo: PreferencesSyncDataInfo,
        val cachedDataInfo: PreferencesSyncDataInfo?,
        val remoteDataInfo: PreferencesSyncDataInfo
    ) : SyncStateRefreshResult

    data class Error(
        val issue: ApiRequestIssue
    ) : SyncStateRefreshResult

}


class DefaultRefreshSyncStateUseCase(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val getLocalSyncDataInfoUseCase: GetLocalSyncDataInfoUseCase,
    private val networkApi: NetworkApi
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

        val remoteSyncDataInfo = PreferencesSyncDataInfo(
            dataId = remoteApiSyncDataInfo.dataId,
            dataVersion = remoteApiSyncDataInfo.dataVersion,
            dataTimestamp = remoteApiSyncDataInfo.dataTimestamp
        )

        val cachedRemoteSyncDataInfo = appPreferences.lastSyncedDataInfo.get()

        fun isEqual() = remoteSyncDataInfo == localSyncDataInfo

        fun isRemoteNotSupported() = remoteSyncDataInfo.dataVersion > CurrentSyncDataVersion

        fun isRemoteNewer() = remoteSyncDataInfo.dataId == localSyncDataInfo.dataId &&
                cachedRemoteSyncDataInfo == localSyncDataInfo &&
                run {
                    val remoteTimestamp = remoteSyncDataInfo.dataTimestamp ?: return@run false
                    val localTimestamp = localSyncDataInfo.dataTimestamp ?: return@run false
                    remoteTimestamp > localTimestamp
                }

        fun isRemoteDataChangedSinceLastSync() = cachedRemoteSyncDataInfo
            ?.equals(remoteSyncDataInfo) == false

        fun isLocalNewer() = remoteSyncDataInfo.dataId == localSyncDataInfo.dataId && run {
            val remoteTimestamp = remoteSyncDataInfo.dataTimestamp ?: return@run false
            val localTimestamp = localSyncDataInfo.dataTimestamp ?: return@run false
            localTimestamp > remoteTimestamp
        }

        val diffType = when {
            isEqual() -> SyncDataDiffType.Equal
            isRemoteNotSupported() -> SyncDataDiffType.RemoteUnsupported
            isRemoteNewer() -> SyncDataDiffType.RemoteNewer
            isRemoteDataChangedSinceLastSync() -> SyncDataDiffType.Incompatible
            isLocalNewer() -> SyncDataDiffType.LocalNewer
            else -> SyncDataDiffType.Incompatible
        }

        return SyncStateRefreshResult.WithRemoteData(
            diffType = diffType,
            localDataInfo = localSyncDataInfo,
            cachedDataInfo = cachedRemoteSyncDataInfo,
            remoteDataInfo = remoteSyncDataInfo,
        )
    }

}