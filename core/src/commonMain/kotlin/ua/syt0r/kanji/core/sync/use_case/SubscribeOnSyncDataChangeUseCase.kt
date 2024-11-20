package ua.syt0r.kanji.core.sync.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.sync.SyncDataInfo
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface SubscribeOnSyncDataChangeUseCase {
    operator fun invoke(state: StateFlow<SyncState>): Flow<Boolean>
}

class DefaultSubscribeOnSyncDataChangeUseCase(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val getLocalSyncDataInfoUseCase: GetLocalSyncDataInfoUseCase
) : SubscribeOnSyncDataChangeUseCase {

    override fun invoke(
        state: StateFlow<SyncState>
    ): Flow<Boolean> {
        return state.flatMapLatest { syncState ->
            if (syncState is SyncState.Disabled) return@flatMapLatest flow { }

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

            combine(
                lastSyncDataInfoFlow,
                localSyncDataInfoFlow
            ) { lastSyncDataInfo, currentSyncDataInfo ->
                lastSyncDataInfo == currentSyncDataInfo
            }
        }
    }

}
