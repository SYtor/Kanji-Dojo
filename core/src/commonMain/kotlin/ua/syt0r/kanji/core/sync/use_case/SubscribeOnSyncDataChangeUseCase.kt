package ua.syt0r.kanji.core.sync.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import ua.syt0r.kanji.core.sync.ApiBackupInfo
import ua.syt0r.kanji.core.user_data.db.UserDataDatabase
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.SyncPropertiesObservable

interface SubscribeOnSyncDataChangeUseCase {
    operator fun invoke(): Flow<ApiBackupInfo>
}

class DefaultSubscribeOnSyncDataChangeUseCase(
    private val syncPropertiesObservable: SyncPropertiesObservable,
    private val letterPracticeRepository: LetterPracticeRepository,
    private val vocabPracticeRepository: VocabPracticeRepository,
    private val appPreferences: PreferencesContract.AppPreferences
) : SubscribeOnSyncDataChangeUseCase {

    override fun invoke(): Flow<ApiBackupInfo> {
        return merge(
            syncPropertiesObservable.changeEvents,
            letterPracticeRepository.changesFlow,
            vocabPracticeRepository.changesFlow,
            flowOf(Unit)
        ).map {
            ApiBackupInfo(
                dataId = appPreferences.localDataId.get(),
                dataVersion = UserDataDatabase.Schema.version,
                dataTimestamp = appPreferences.localDataTimestamp.get()
            )
        }
    }

}
