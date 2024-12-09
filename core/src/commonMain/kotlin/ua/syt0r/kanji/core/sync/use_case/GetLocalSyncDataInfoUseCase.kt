package ua.syt0r.kanji.core.sync.use_case

import ua.syt0r.kanji.core.sync.SyncDataInfo
import ua.syt0r.kanji.core.user_data.db.UserDataDatabase
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface GetLocalSyncDataInfoUseCase {
    suspend operator fun invoke(): SyncDataInfo
}

class DefaultGetLocalSyncDataInfoUseCase(
    private val appPreferences: PreferencesContract.AppPreferences
) : GetLocalSyncDataInfoUseCase {

    override suspend fun invoke(): SyncDataInfo {
        return SyncDataInfo(
            dataId = appPreferences.localDataId.get(),
            dataVersion = UserDataDatabase.Schema.version,
            dataTimestamp = appPreferences.localDataTimestamp.get()?.toEpochMilliseconds()
        )
    }

}