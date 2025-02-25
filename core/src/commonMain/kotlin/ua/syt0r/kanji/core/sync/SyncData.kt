package ua.syt0r.kanji.core.sync

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.user_data.db.UserDataDatabase
import ua.syt0r.kanji.core.user_data.preferences.PreferencesSyncDataInfo

sealed interface SyncFeatureState {

    object Loading : SyncFeatureState

    object Disabled : SyncFeatureState

    interface Enabled : SyncFeatureState {
        val state: StateFlow<SyncState>
        fun sync(): CompletableDeferred<SyncState>
        fun cancel()
        fun resolveConflict(strategy: SyncConflictResolveStrategy)
    }

    data class Error(
        val issue: ApiRequestIssue
    ) : SyncFeatureState

}

sealed interface SyncState {

    object Refreshing : SyncState

    sealed interface TrackingChanges : SyncState {
        val uploadAvailable: StateFlow<Boolean>

        object NoRemoteData : TrackingChanges {
            override val uploadAvailable: StateFlow<Boolean> = MutableStateFlow(true)
        }

        data class WithRemoteData(
            override val uploadAvailable: StateFlow<Boolean>
        ) : TrackingChanges

    }

    object Uploading : SyncState

    object Downloading : SyncState

    object Canceled : SyncState

    data class Conflict(
        val diffType: SyncDataDiffType,
        val remoteDataInfo: PreferencesSyncDataInfo,
        val localDataInfo: PreferencesSyncDataInfo,
        val cachedDataInfo: PreferencesSyncDataInfo?
    ) : SyncState

    sealed interface Error : SyncState {
        data class Api(val issue: ApiRequestIssue) : Error
    }

}

sealed interface SyncIntent {
    object Refresh : SyncIntent
    object Sync : SyncIntent
    data class ResolveConflict(val strategy: SyncConflictResolveStrategy) : SyncIntent
    object Cancel : SyncIntent
}

enum class SyncConflictResolveStrategy { UploadLocal, DownloadRemote }

enum class SyncDataDiffType { Equal, LocalNewer, RemoteNewer, Incompatible, RemoteUnsupported }

val CurrentSyncDataVersion = UserDataDatabase.Schema.version
