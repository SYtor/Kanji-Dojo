package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.sync.SyncConflictResolveStrategy
import ua.syt0r.kanji.core.sync.SyncDataDiffType
import ua.syt0r.kanji.core.sync.SyncFeatureState
import ua.syt0r.kanji.core.sync.SyncManager
import ua.syt0r.kanji.core.sync.SyncState

class MainScreenViewModel(
    viewModelScope: CoroutineScope,
    private val syncManager: SyncManager
) : MainContract.ViewModel {

    private val _syncDialogState = MutableStateFlow<SyncDialogState>(SyncDialogState.Hidden)
    override val syncDialogState: StateFlow<SyncDialogState> = _syncDialogState

    init {
        syncManager.state.toDialogState()
            .onEach { _syncDialogState.value = it }
            .launchIn(viewModelScope)
    }

    override fun cancelSync() {
        when (val syncState = syncManager.state.value) {
            SyncFeatureState.Disabled,
            SyncFeatureState.Loading -> Unit

            is SyncFeatureState.Enabled -> syncState.cancel()
            is SyncFeatureState.Error -> {
                val error = _syncDialogState.value as? SyncDialogState.Error
                error?.showDialog?.value = false
            }
        }
    }

    override fun resolveConflict(strategy: SyncConflictResolveStrategy) {
        when (val syncState = syncManager.state.value) {
            is SyncFeatureState.Enabled -> syncState.resolveConflict(strategy)
            else -> Unit
        }
    }

    private fun StateFlow<SyncFeatureState>.toDialogState(): Flow<SyncDialogState> {
        return flatMapLatest { syncFeatureState ->
            when (syncFeatureState) {
                SyncFeatureState.Disabled,
                SyncFeatureState.Loading -> flowOf(SyncDialogState.Hidden)

                is SyncFeatureState.Error -> flowOf(
                    SyncDialogState.Error.Api(
                        showDialog = mutableStateOf(false),
                        issue = syncFeatureState.issue
                    )
                )

                is SyncFeatureState.Enabled -> syncFeatureState.state.map { syncState ->
                    when (syncState) {
                        SyncState.Refreshing,
                        SyncState.Canceled,
                        is SyncState.TrackingChanges -> SyncDialogState.Hidden

                        SyncState.Uploading -> SyncDialogState.Uploading
                        SyncState.Downloading -> SyncDialogState.Downloading
                        is SyncState.Conflict -> {
                            when (syncState.diffType) {
                                SyncDataDiffType.RemoteUnsupported -> {
                                    SyncDialogState.Error.Unsupported(
                                        showDialog = mutableStateOf(false)
                                    )
                                }

                                else -> SyncDialogState.Conflict(
                                    diffType = syncState.diffType,
                                    remoteDataTime = syncState.remoteDataInfo.dataTimestamp
                                        ?.let { Instant.fromEpochMilliseconds(it) }
                                        ?.toLocalDateTime(TimeZone.currentSystemDefault()),
                                    lastSyncTime = syncState.cachedDataInfo?.dataTimestamp
                                        ?.let { Instant.fromEpochMilliseconds(it) }
                                        ?.toLocalDateTime(TimeZone.currentSystemDefault())
                                )
                            }
                        }

                        is SyncState.Error.Api -> SyncDialogState.Error.Api(
                            showDialog = mutableStateOf(false),
                            issue = syncState.issue
                        )
                    }
                }
            }
        }
    }

}
