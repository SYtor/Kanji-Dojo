package ua.syt0r.kanji.core.sync.use_case

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.sync.SyncDataInfo
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface CreateTrackingChangesSyncStateUseCase {

    suspend operator fun invoke(
        coroutineScope: CoroutineScope
    ): SyncState.TrackingChanges.WithRemoteData

}

class DefaultCreateTrackingChangesSyncStateUseCase(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val getLocalSyncDataInfoUseCase: GetLocalSyncDataInfoUseCase
) : CreateTrackingChangesSyncStateUseCase {

    override suspend fun invoke(
        coroutineScope: CoroutineScope
    ): SyncState.TrackingChanges.WithRemoteData {
        val lastSyncDataInfoFlow = appPreferences.lastSyncedDataInfoJson.onModified
            .onStart { emit(appPreferences.lastSyncedDataInfoJson.get()) }
            .map { it?.let { Json.decodeFromString<SyncDataInfo>(it) } }

        val localSyncDataInfoFlow = merge(
            appPreferences.localDataId.onModified,
            appPreferences.localDataTimestamp.onModified
        )
            .map { Unit }
            .onStart { emit(Unit) }
            .map { getLocalSyncDataInfoUseCase() }
            .stateIn(coroutineScope)

        val uploadAvailable = combine(
            lastSyncDataInfoFlow,
            localSyncDataInfoFlow
        ) { lastSyncDataInfo, currentSyncDataInfo ->
            lastSyncDataInfo != currentSyncDataInfo
        }.stateIn(coroutineScope)

        return SyncState.TrackingChanges.WithRemoteData(
            uploadAvailable = uploadAvailable
        )
    }

}
