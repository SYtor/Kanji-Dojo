package ua.syt0r.kanji.core.sync.use_case

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.SyncConflictResolveStrategy
import ua.syt0r.kanji.core.sync.SyncDataDiffType
import ua.syt0r.kanji.core.sync.SyncIntent
import ua.syt0r.kanji.core.sync.SyncState

interface HandleSyncIntentUseCase {
    suspend operator fun invoke(coroutineScope: CoroutineScope, intent: SyncIntent): Flow<SyncState>
}

class DefaultHandleSyncIntentUseCase(
    private val refreshSyncStateUseCase: RefreshSyncStateUseCase,
    private val uploadSyncDataUseCase: UploadSyncDataUseCase,
    private val applyRemoteSyncDataUseCase: ApplyRemoteSyncDataUseCase,
    private val createTrackingChangesSyncStateUseCase: CreateTrackingChangesSyncStateUseCase,
) : HandleSyncIntentUseCase {

    override suspend fun invoke(
        coroutineScope: CoroutineScope,
        intent: SyncIntent
    ): Flow<SyncState> {

        return when (intent) {
            SyncIntent.Cancel -> flowOf(SyncState.Canceled)
            SyncIntent.Refresh -> flow {
                emit(SyncState.Refreshing)
                Logger.d("Handling refresh intent")

                val refreshResult = refreshSyncStateUseCase()
                Logger.d("Refresh sync, refreshResult[$refreshResult]")

                refreshResult.asConflictSyncState()?.let {
                    emit(it)
                    return@flow
                }

                val updatedSyncState = refreshResult.toActiveSyncState(coroutineScope)
                emit(updatedSyncState)
            }

            SyncIntent.Sync -> flow {
                emit(SyncState.Refreshing)
                Logger.d("Handling sync intent")

                val refreshResult = refreshSyncStateUseCase()
                Logger.d("Refresh sync, refreshResult[$refreshResult]")

                val isNoRemoteData = refreshResult is SyncStateRefreshResult.NoRemoteData
                val isRemoteDataNewer = refreshResult is SyncStateRefreshResult.WithRemoteData &&
                        refreshResult.diffType == SyncDataDiffType.LocalNewer

                val shouldUpload = isNoRemoteData || isRemoteDataNewer

                if (shouldUpload) {
                    handleUpload(coroutineScope)
                    return@flow
                }

                refreshResult.asConflictSyncState()?.let {
                    emit(it)
                    return@flow
                }

                if (
                    refreshResult is SyncStateRefreshResult.WithRemoteData &&
                    refreshResult.diffType == SyncDataDiffType.RemoteNewer
                ) {
                    handleDownload(coroutineScope)
                    return@flow
                }

                Logger.d("Data is the same, nothing to sync")
                val updatedSyncState = refreshResult.toActiveSyncState(coroutineScope)
                emit(updatedSyncState)

            }

            is SyncIntent.ResolveConflict -> flow {
                Logger.d("Handling resolve conflict intent")
                when (intent.strategy) {
                    SyncConflictResolveStrategy.UploadLocal -> {
                        handleUpload(coroutineScope)
                    }

                    SyncConflictResolveStrategy.DownloadRemote -> {
                        handleDownload(coroutineScope)
                    }
                }
            }

        }

    }

    private suspend fun FlowCollector<SyncState>.handleUpload(coroutineScope: CoroutineScope) {
        Logger.logMethod()
        emit(SyncState.Uploading)

        val uploadResult = uploadSyncDataUseCase()
        Logger.d("uploadResult[$uploadResult]")

        val updatedSyncState = when (uploadResult) {
            UploadSyncDataResult.Success -> createTrackingChangesSyncStateUseCase(coroutineScope)
            is UploadSyncDataResult.Fail -> SyncState.Error.Api(uploadResult.issue)
        }

        emit(updatedSyncState)
    }

    private suspend fun FlowCollector<SyncState>.handleDownload(coroutineScope: CoroutineScope) {
        Logger.logMethod()
        emit(SyncState.Downloading)

        val applySyncResult = applyRemoteSyncDataUseCase()
        Logger.d("applySyncResult[$applySyncResult]")

        val updatedSyncState = when (applySyncResult) {
            ApplySyncResult.Success -> createTrackingChangesSyncStateUseCase(coroutineScope)
            is ApplySyncResult.Fail -> SyncState.Error.Api(applySyncResult.issue)
        }

        emit(updatedSyncState)
    }

    private suspend fun SyncStateRefreshResult.toActiveSyncState(
        coroutineScope: CoroutineScope
    ): SyncState {
        return when (this) {
            is SyncStateRefreshResult.NoRemoteData -> {
                SyncState.TrackingChanges.NoRemoteData
            }

            is SyncStateRefreshResult.WithRemoteData -> {
                createTrackingChangesSyncStateUseCase(coroutineScope)
            }

            is SyncStateRefreshResult.Error -> SyncState.Error.Api(issue)
        }
    }

    private fun SyncStateRefreshResult.asConflictSyncState(): SyncState.Conflict? {
        return takeIf {
            it is SyncStateRefreshResult.WithRemoteData && it.diffType != SyncDataDiffType.Equal
        }?.run {
            this as SyncStateRefreshResult.WithRemoteData
            SyncState.Conflict(
                diffType = diffType,
                remoteDataInfo = remoteDataInfo,
                localDataInfo = localDataInfo,
                cachedDataInfo = cachedDataInfo
            )
        }
    }

}