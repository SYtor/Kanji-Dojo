package ua.syt0r.kanji.core.user_data.preferences

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.srs.DailyLimitConfiguration
import ua.syt0r.kanji.core.srs.PracticeLimit
import ua.syt0r.kanji.core.suspended_property.DataStoreSuspendedPropertyProvider
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyProvider

interface UserPreferencesManager {

    val suspendedPropertyProvider: SuspendedPropertyProvider

    suspend fun clear()
    suspend fun migrate()

}

class DataStoreUserPreferencesManager(
    private val dataStore: DataStore<Preferences>,
    private val migrations: List<DataMigration<Preferences>>
) : UserPreferencesManager {

    companion object {
        val DefaultMigrations: List<DataMigration<Preferences>> = listOf(DailyLimitDataMigration)
    }

    override val suspendedPropertyProvider: SuspendedPropertyProvider =
        DataStoreSuspendedPropertyProvider(dataStore)

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    override suspend fun migrate() {
        val cleanUps = mutableListOf<suspend () -> Unit>()

        dataStore.updateData { startingData ->
            migrations.fold(startingData) { data, migration ->
                if (migration.shouldMigrate(data)) {
                    cleanUps.add { migration.cleanUp() }
                    migration.migrate(data)
                } else {
                    data
                }
            }
        }

        var cleanUpFailure: Throwable? = null

        cleanUps.forEach { cleanUp ->
            try {
                cleanUp()
            } catch (exception: Throwable) {
                if (cleanUpFailure == null) {
                    cleanUpFailure = exception
                } else {
                    cleanUpFailure!!.addSuppressed(exception)
                }
            }
        }

        // If we encountered a failure on cleanup, throw it.
        cleanUpFailure?.let { throw it }
    }

}

private object DailyLimitDataMigration : DataMigration<Preferences> {

    private val legacyNewLimitKey = intPreferencesKey("daily_learn_limit")
    private val legacyDueLimitKey = intPreferencesKey("daily_review_limit")
    private val legacySortByTimeKey = booleanPreferencesKey("dashboard_sort_by_time")
    private val legacyKeys = listOf(legacyNewLimitKey, legacyDueLimitKey, legacySortByTimeKey)

    private val dailyLimitConfigurationKey = stringPreferencesKey("daily_limit_configuration")
    private val letterDashboardSortByTimeKey =
        booleanPreferencesKey("letter_dashboard_sort_by_time")
    private val vocabDashboardSortByTimeKey =
        booleanPreferencesKey("vocab_dashboard_sort_by_time")
    private val json = Json { encodeDefaults = true }

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        return legacyKeys.any { currentData.contains(it) }
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val mutablePreferences = currentData.toMutablePreferences()

        val defaultLimit = PracticeLimit()
        val legacyNew = mutablePreferences[legacyNewLimitKey] ?: defaultLimit.new
        val legacyDue = mutablePreferences[legacyDueLimitKey] ?: defaultLimit.due
        val legacySortByTime = mutablePreferences[legacySortByTimeKey] ?: false

        val dailyLimitConfiguration = DailyLimitConfiguration(
            letterCombinedLimit = PracticeLimit(legacyNew, legacyDue)
        )
        val dailyLimitConfigurationJson = json.encodeToString(dailyLimitConfiguration)

        mutablePreferences[dailyLimitConfigurationKey] = dailyLimitConfigurationJson
        mutablePreferences[letterDashboardSortByTimeKey] = legacySortByTime
        mutablePreferences[vocabDashboardSortByTimeKey] = legacySortByTime

        legacyKeys.forEach { mutablePreferences.remove(it) }

        return mutablePreferences
    }

    override suspend fun cleanUp() {}

}
