package ua.syt0r.kanji.core.sync

import io.ktor.http.HttpStatusCode
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


sealed interface SyncState {

    object Disabled : SyncState

    object Refreshing : SyncState
    object NoChanges : SyncState
    object PendingUpload : SyncState
    object Syncing : SyncState
    object Canceled : SyncState

    data class Conflict(
        val remoteTime: Instant?
    ) : SyncState

    sealed interface Error : SyncState

    object AuthExpired : Error
    object MissingSubscription : Error
    object MissingConnection : Error
    data class Fail(val throwable: Throwable) : Error

}

enum class SyncConflictResolveStrategy { UploadLocal, DownloadRemote }

@Serializable
data class ApiBackupInfo(
    val dataId: String,
    val dataVersion: Long,
    val dataTimestamp: Long?
)

data class HttpResponseException(
    val statusCode: HttpStatusCode
) : Throwable()