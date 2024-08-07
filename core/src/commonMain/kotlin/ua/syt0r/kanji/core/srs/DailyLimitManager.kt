package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository

interface DailyLimitManager {
    val changesFlow: SharedFlow<Unit>
    suspend fun getConfiguration(): DailyLimitConfiguration
    suspend fun updateConfiguration(configuration: DailyLimitConfiguration)
}

data class DailyLimitConfiguration(
    val enabled: Boolean,
    val newLimit: Int,
    val dueLimit: Int
)

class DefaultDailyLimitManager(
    private val userPreferencesRepository: UserPreferencesRepository
) : DailyLimitManager {

    private val _changesFlow = MutableSharedFlow<Unit>()
    override val changesFlow: SharedFlow<Unit> = _changesFlow // TODO migration?

    override suspend fun getConfiguration(): DailyLimitConfiguration {
        return DailyLimitConfiguration(
            enabled = userPreferencesRepository.dailyLimitEnabled.get(),
            newLimit = userPreferencesRepository.dailyNewLimit.get(),
            dueLimit = userPreferencesRepository.dailyDueLimit.get()
        )
    }

    override suspend fun updateConfiguration(configuration: DailyLimitConfiguration) {
        userPreferencesRepository.apply {
            userPreferencesRepository.dailyLimitEnabled.set(configuration.enabled)
            userPreferencesRepository.dailyNewLimit.set(configuration.newLimit)
            userPreferencesRepository.dailyDueLimit.set(configuration.dueLimit)
        }
        _changesFlow.emit(Unit)
    }
}