package ua.syt0r.kanji.core.suspended_property

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonPrimitive


class DataStoreSuspendedProperty<ExposedType, BackingType>(
    private val dataStore: DataStore<Preferences>,
    private val type: SuspendedPropertyType<ExposedType, BackingType>,
    override val key: String,
    private val saveInitialValue: Boolean,
    private val initialValueProvider: () -> ExposedType
) : SuspendedProperty<ExposedType> {

    private val dataStoreKey = type.createKey(key)

    private val _onModifier = MutableSharedFlow<ExposedType>()
    override val onModified: SharedFlow<ExposedType> = _onModifier

    override suspend fun isModified(): Boolean {
        return dataStore.data.first().contains(dataStoreKey)
    }

    override suspend fun get(): ExposedType {
        return dataStore.data
            .first()[dataStoreKey]
            ?.let { type.convertToExposed(it) }
            ?: initialValueProvider().also { value ->
                if (saveInitialValue) internalSet(type.convertToBacking(value))
            }
    }

    override suspend fun set(value: ExposedType) {
        internalSet(value = type.convertToBacking(value))
        _onModifier.emit(value)
    }

    private suspend fun internalSet(value: BackingType) {
        dataStore.edit { it[dataStoreKey] = value }
    }

    override suspend fun backup(): JsonPrimitive = type.backup(get())

    override suspend fun restore(value: JsonPrimitive) {
        internalSet(value = type.convertToBacking(type.restore(value)))
    }

}

class NullableDataStoreSuspendedProperty<ExposedType, BackingType>(
    private val dataStore: DataStore<Preferences>,
    private val type: SuspendedPropertyType<ExposedType, BackingType>,
    override val key: String,
    private val saveInitialValue: Boolean,
    private val initialValueProvider: () -> ExposedType?
) : SuspendedProperty<ExposedType?> {

    private val dataStoreKey = type.createKey(key)

    private val _onModifier = MutableSharedFlow<ExposedType?>()
    override val onModified: SharedFlow<ExposedType?> = _onModifier

    override suspend fun isModified(): Boolean {
        return dataStore.data.first().contains(dataStoreKey)
    }

    override suspend fun get(): ExposedType? {
        return dataStore.data
            .first()[dataStoreKey]
            ?.let { type.convertToExposed(it) }
            ?: initialValueProvider()?.also { value ->
                if (saveInitialValue) internalSet(type.convertToBacking(value))
            }
    }

    override suspend fun set(value: ExposedType?) {
        internalSet(value = value?.let(type::convertToBacking))
        _onModifier.emit(value)
    }

    private suspend fun internalSet(value: BackingType?) {
        dataStore.edit {
            if (value != null) it[dataStoreKey] = value
            else it.remove(dataStoreKey)
        }
    }

    override suspend fun backup(): JsonPrimitive? = get()?.let(type::backup)
    override suspend fun restore(value: JsonPrimitive) {
        internalSet(value = type.convertToBacking(type.restore(value)))
    }

}
