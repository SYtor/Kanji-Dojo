package ua.syt0r.kanji.core.suspended_property

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long

private class DataStoreSuspendedProperty<T>(
    private val dataStore: DataStore<Preferences>,
    val dataStoreKey: Preferences.Key<T>,
    val restoreParser: JsonPrimitive.() -> T,
    private val initialValueProvider: () -> T
) : SuspendedProperty<T> {

    override val key: String = dataStoreKey.name

    override suspend fun isModified(): Boolean {
        return dataStore.data.first().contains(dataStoreKey)
    }

    override suspend fun get(): T {
        return dataStore.data.first()[dataStoreKey] ?: initialValueProvider()
    }

    override suspend fun set(value: T) {
        dataStore.edit { it[dataStoreKey] = value }
    }

    override suspend fun backup(): JsonPrimitive = get().toJsonPrimitive()
    override suspend fun restore(value: JsonPrimitive) = set(value.restoreParser())

}

private class NullableDataStoreSuspendedProperty<T>(
    private val dataStore: DataStore<Preferences>,
    val dataStoreKey: Preferences.Key<T>,
    val restoreParser: JsonPrimitive.() -> T,
    private val initialValueProvider: () -> T?
) : SuspendedProperty<T?> {

    override val key: String = dataStoreKey.name

    override suspend fun isModified(): Boolean {
        return dataStore.data.first().contains(dataStoreKey)
    }

    override suspend fun get(): T? {
        val data = dataStore.data.first()
        return when {
            data.contains(dataStoreKey) -> data[dataStoreKey]
            else -> initialValueProvider()
        }
    }

    override suspend fun set(value: T?) {
        dataStore.edit {
            if (value == null) it.remove(dataStoreKey)
            else it[dataStoreKey] = value
        }
    }

    override suspend fun backup(): JsonPrimitive = get()!!.toJsonPrimitive()
    override suspend fun restore(value: JsonPrimitive) = set(value.restoreParser())

}

private fun <T> T.toJsonPrimitive(): JsonPrimitive {
    return when (this) {
        is Boolean -> JsonPrimitive(this)
        is Int -> JsonPrimitive(this)
        is Long -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        else -> error("Unsupported type")
    }
}

private inline fun <reified T> JsonPrimitive.getValue(): T {
    return when (T::class) {
        Boolean::class -> boolean as T
        Int::class -> int as T
        Long::class -> long as T
        String::class -> content as T
        else -> error("Unsupported type")
    }
}

class DataStoreSuspendedPropertyProvider(
    private val dataStore: DataStore<Preferences>,
) : SuspendedPropertyProvider {

    override fun createBooleanProperty(
        key: String,
        initialValueProvider: () -> Boolean
    ): SuspendedProperty<Boolean> {
        return DataStoreSuspendedProperty(
            dataStore = dataStore,
            dataStoreKey = booleanPreferencesKey(key),
            restoreParser = { getValue() },
            initialValueProvider = initialValueProvider
        )
    }

    override fun createIntProperty(
        key: String,
        initialValueProvider: () -> Int
    ): SuspendedProperty<Int> {
        return DataStoreSuspendedProperty(
            dataStore = dataStore,
            dataStoreKey = intPreferencesKey(key),
            restoreParser = { getValue() },
            initialValueProvider = initialValueProvider
        )
    }

    override fun createLongProperty(
        key: String,
        initialValueProvider: () -> Long
    ): SuspendedProperty<Long> {
        return DataStoreSuspendedProperty(
            dataStore = dataStore,
            dataStoreKey = longPreferencesKey(key),
            restoreParser = { getValue() },
            initialValueProvider = initialValueProvider
        )
    }

    override fun createNullableLongProperty(
        key: String,
        initialValueProvider: () -> Long?
    ): SuspendedProperty<Long?> {
        return NullableDataStoreSuspendedProperty(
            dataStore = dataStore,
            dataStoreKey = longPreferencesKey(key),
            restoreParser = { getValue() },
            initialValueProvider = initialValueProvider
        )
    }

    override fun createStringProperty(
        key: String,
        initialValueProvider: () -> String
    ): SuspendedProperty<String> {
        return DataStoreSuspendedProperty(
            dataStore = dataStore,
            dataStoreKey = stringPreferencesKey(key),
            restoreParser = { getValue() },
            initialValueProvider = initialValueProvider
        )
    }

    override fun createNullableStringProperty(
        key: String,
        initialValueProvider: () -> String?
    ): SuspendedProperty<String?> {
        return NullableDataStoreSuspendedProperty(
            dataStore = dataStore,
            dataStoreKey = stringPreferencesKey(key),
            restoreParser = { getValue() },
            initialValueProvider = initialValueProvider
        )
    }

}