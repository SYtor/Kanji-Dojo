package ua.syt0r.kanji.core.user_data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.suspended_property.DataStoreSuspendedProperty
import ua.syt0r.kanji.core.suspended_property.NullableDataStoreSuspendedProperty
import ua.syt0r.kanji.core.suspended_property.SuspendedProperty
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyCreatorScope
import ua.syt0r.kanji.core.suspended_property.SuspendedPropertyType
import ua.syt0r.kanji.core.time.TimeUtils

interface PreferencesManager {

    val appPreferences: PreferencesContract.AppPreferences
    val practicePreferences: PreferencesContract.PracticePreferences

    suspend fun clear()
    suspend fun migrate()

}

interface BackupPropertiesHolder {
    val backupProperties: List<SuspendedProperty<*>>
}

class DataStorePreferencesManager(
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>,
    private val timeUtils: TimeUtils,
    private val migrationManager: UserPreferencesMigrationManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
) : PreferencesManager,
    BackupPropertiesHolder,
    SuspendedPropertyCreatorScope {

    private val _backupProperties = mutableSetOf<SuspendedProperty<*>>()
    override val backupProperties: List<SuspendedProperty<*>>
        get() = _backupProperties.toList()

    override val appPreferences: PreferencesContract.AppPreferences = AppPreferences(this)
    override val practicePreferences: PreferencesContract.PracticePreferences =
        PracticePreferences(this)

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    override suspend fun migrate() {
        migrationManager.migrate()
    }

    override fun <T1, T2> createProperty(
        type: SuspendedPropertyType<T1, T2>,
        key: String,
        enableBackup: Boolean,
        saveInitialValue: Boolean,
        affectSync: Boolean,
        initialValue: () -> T1
    ): SuspendedProperty<T1> {
        val property = DataStoreSuspendedProperty(
            dataStore = dataStore,
            type = type,
            key = key,
            saveInitialValue = saveInitialValue,
            initialValueProvider = initialValue
        )

        if (enableBackup) _backupProperties.add(property)
        if (affectSync) coroutineScope.launch { handleSyncAffectingPropertyUpdate(property) }
        return property
    }

    override fun <T1, T2> createNullableProperty(
        type: SuspendedPropertyType<T1, T2>,
        key: String,
        enableBackup: Boolean,
        saveInitialValue: Boolean,
        affectSync: Boolean,
        initialValue: () -> T1?
    ): SuspendedProperty<T1?> {
        val property = NullableDataStoreSuspendedProperty(
            dataStore = dataStore,
            type = type,
            key = key,
            saveInitialValue = saveInitialValue,
            initialValueProvider = initialValue
        )

        if (enableBackup) _backupProperties.add(property)
        if (affectSync)
            coroutineScope.launch { handleSyncAffectingPropertyUpdate(property) }
        return property
    }

    private suspend fun handleSyncAffectingPropertyUpdate(property: SuspendedProperty<*>) {
        property.onModified
            .onEach {
                val time = timeUtils.now().toEpochMilliseconds()
                appPreferences.localDataTimestamp.set(time)
            }
            .collect()
    }

}
