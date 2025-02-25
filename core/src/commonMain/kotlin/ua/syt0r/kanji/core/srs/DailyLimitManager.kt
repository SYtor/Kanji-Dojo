package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract

interface DailyLimitManager {

    val changesFlow: SharedFlow<Unit>

    suspend fun isEnabled(): Boolean
    suspend fun getConfiguration(): DailyLimitConfiguration

    suspend fun update(isEnabled: Boolean, configuration: DailyLimitConfiguration)

}

@Serializable
data class PracticeLimit(
    val new: Int = 4,
    val due: Int = 60
)

val DefaultPracticeLimit = PracticeLimit(
    new = 4,
    due = 60
)

@Serializable
data class DailyLimitConfiguration(
    val isLetterLimitCombined: Boolean = true,
    val letterCombinedLimit: PracticeLimit = DefaultPracticeLimit,
    val letterSeparatedLimit: Map<LetterPracticeType, PracticeLimit> = LetterPracticeType.values()
        .associateWith { DefaultPracticeLimit },
    val isVocabLimitCombined: Boolean = true,
    val vocabCombinedLimit: PracticeLimit = DefaultPracticeLimit,
    val vocabSeparatedLimit: Map<VocabPracticeType, PracticeLimit> = VocabPracticeType.values()
        .associateWith { DefaultPracticeLimit }
)

class DefaultDailyLimitManager(
    private val appPreferences: PreferencesContract.AppPreferences
) : DailyLimitManager {

    private val json = Json { encodeDefaults = true }

    private val _changesFlow = MutableSharedFlow<Unit>()
    override val changesFlow: SharedFlow<Unit> = _changesFlow // TODO migration?

    override suspend fun isEnabled(): Boolean {
        return appPreferences.dailyLimitEnabled.get()
    }

    override suspend fun getConfiguration(): DailyLimitConfiguration {
        val configurationJson = appPreferences.dailyLimitConfigurationJson.get()
        val configuration = Json
            .runCatching { decodeFromString<DailyLimitConfiguration>(configurationJson) }
            .getOrElse { null }
            ?: DailyLimitConfiguration()

        return configuration
    }

    override suspend fun update(isEnabled: Boolean, configuration: DailyLimitConfiguration) {
        appPreferences.dailyLimitEnabled.set(isEnabled)
        val configurationJson = json.encodeToString(configuration)
        appPreferences.dailyLimitConfigurationJson.set(configurationJson)
        _changesFlow.emit(Unit)
    }

}