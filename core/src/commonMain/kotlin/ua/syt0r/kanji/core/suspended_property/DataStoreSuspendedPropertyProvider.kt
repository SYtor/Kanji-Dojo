package ua.syt0r.kanji.core.suspended_property

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

private abstract class DataStoreSuspendedProperty<T>(
    private val dataStore: DataStore<Preferences>,
    override val key: String,
    private val initialValueProvider: () -> T
) : SuspendedProperty<T> {

    abstract val dataStoreKey: Preferences.Key<T>

    override suspend fun isModified(): Boolean {
        return dataStore.data.first().contains(dataStoreKey)
    }

    override suspend fun get(): T {
        return dataStore.data.first()[dataStoreKey] ?: initialValueProvider()
    }

    override suspend fun set(value: T) {
        dataStore.edit { it[dataStoreKey] = value }
    }

}

class DataStoreSuspendedPropertyProvider(
    private val dataStore: DataStore<Preferences>,
) : SuspendedPropertyProvider {

    override fun createBooleanProperty(
        key: String,
        initialValueProvider: () -> Boolean
    ): SuspendedProperty<Boolean> {
        return object : DataStoreSuspendedProperty<Boolean>(
            dataStore = dataStore,
            key = key,
            initialValueProvider = initialValueProvider
        ), BooleanSuspendedProperty {

            override val dataStoreKey: Preferences.Key<Boolean> = booleanPreferencesKey(key)

        }
    }

    override fun createIntProperty(
        key: String,
        initialValueProvider: () -> Int
    ): SuspendedProperty<Int> {
        return object : DataStoreSuspendedProperty<Int>(
            dataStore = dataStore,
            key = key,
            initialValueProvider = initialValueProvider
        ), IntegerSuspendedProperty {

            override val dataStoreKey: Preferences.Key<Int> = intPreferencesKey(key)

        }
    }

    override fun createLongProperty(
        key: String,
        initialValueProvider: () -> Long
    ): SuspendedProperty<Long> {
        return object : DataStoreSuspendedProperty<Long>(
            dataStore = dataStore,
            key = key,
            initialValueProvider = initialValueProvider
        ), LongSuspendedProperty {

            override val dataStoreKey: Preferences.Key<Long> = longPreferencesKey(key)

        }
    }

    override fun createStringProperty(
        key: String,
        initialValueProvider: () -> String
    ): SuspendedProperty<String> {
        return object : DataStoreSuspendedProperty<String>(
            dataStore = dataStore,
            key = key,
            initialValueProvider = initialValueProvider
        ), StringSuspendedProperty {

            override val dataStoreKey: Preferences.Key<String> = stringPreferencesKey(key)

        }
    }

}