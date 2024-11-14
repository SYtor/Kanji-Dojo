package ua.syt0r.kanji.core.sync.use_case

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import ua.syt0r.kanji.core.NetworkApi
import ua.syt0r.kanji.core.sync.SyncState

interface GetRemoteBackupUseCase {
    suspend operator fun invoke(): SyncState
}

class DefaultGetRemoteBackupUseCase(
    private val httpClient: HttpClient,
) : GetRemoteBackupUseCase {

    override suspend fun invoke(): SyncState {

        kotlin.runCatching {
            val response = httpClient.get(NetworkApi.Url.GET_BACKUP)
            TODO()
            return SyncState.NoChanges
        }.getOrElse {
            return SyncState.Fail(it)
        }

    }

}