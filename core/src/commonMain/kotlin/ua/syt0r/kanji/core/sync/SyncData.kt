package ua.syt0r.kanji.core.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.user_data.db.UserDataDatabase

sealed interface SyncFeatureState {

    object Loading : SyncFeatureState

    object Disabled : SyncFeatureState

    interface Enabled : SyncFeatureState {
        val state: StateFlow<SyncState>
    }

    object Error : SyncFeatureState

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
        val remoteDataInfo: SyncDataInfo,
        val localDataInfo: SyncDataInfo,
        val cachedDataInfo: SyncDataInfo?
    ) : SyncState

    sealed interface Error : SyncState {
        data class Api(val issue: ApiRequestIssue) : Error
        data class Fail(val throwable: Throwable) : Error
    }

}

class MutableEnabledSyncFeatureState : SyncFeatureState.Enabled {

    private val _state = MutableStateFlow<SyncState>(SyncState.Refreshing)
    override val state: StateFlow<SyncState> = _state

    fun setState(state: SyncState) {
        _state.value = state
    }

}

sealed interface SyncIntent {
    object Refresh : SyncIntent
    object Sync : SyncIntent
    data class ResolveConflict(val strategy: SyncConflictResolveStrategy) : SyncIntent
}

enum class SyncConflictResolveStrategy { UploadLocal, DownloadRemote }

@Serializable
data class SyncDataInfo(
    val dataId: String,
    val dataVersion: Long,
    val dataTimestamp: Long?
)

enum class SyncDataDiffType { Equal, LocalNewer, RemoteNewer, Incompatible, RemoteUnsupported }

val CurrentSyncDataVersion = UserDataDatabase.Schema.version
