package ua.syt0r.kanji.core.sync

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.user_data.db.UserDataDatabase


sealed interface SyncState {

    object Disabled : SyncState
    enum class Enabled : SyncState { UpToDate, PendingUpload }
    object Canceled : SyncState

    enum class Loading(
        val isBlocking: Boolean
    ) : SyncState {
        Refreshing(false),
        Uploading(true),
        Downloading(true)
    }

    data class Conflict(
        val remoteDataInfo: SyncDataInfo,
        val localDataInfo: SyncDataInfo,
        val cachedDataInfo: SyncDataInfo?
    ) : SyncState

    sealed interface Error : SyncState {
        object AuthExpired : Error
        object MissingSubscription : Error
        object MissingConnection : Error
        data class Fail(
            val throwable: Throwable
        ) : Error
    }

}

enum class SyncConflictResolveStrategy { UploadLocal, DownloadRemote }

@Serializable
data class SyncDataInfo(
    val dataId: String,
    val dataVersion: Long,
    val dataTimestamp: Long?
)

val CurrentSyncDataVersion = UserDataDatabase.Schema.version

data class HttpResponseException(
    val statusCode: HttpStatusCode
) : Throwable()