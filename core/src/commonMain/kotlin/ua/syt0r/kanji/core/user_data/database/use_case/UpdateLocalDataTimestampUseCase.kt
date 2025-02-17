package ua.syt0r.kanji.core.user_data.database.use_case

import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface UpdateLocalDataTimestampUseCase {
    suspend operator fun invoke()
}

class DefaultUpdateLocalDataTimestampUseCase(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val timeUtils: TimeUtils
) : UpdateLocalDataTimestampUseCase {

    override suspend fun invoke() {
        appPreferences.localDataTimestamp.set(timeUtils.now())
    }

}