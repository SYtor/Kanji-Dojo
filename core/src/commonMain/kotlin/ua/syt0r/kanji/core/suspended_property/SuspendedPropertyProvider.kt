package ua.syt0r.kanji.core.suspended_property

import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.JsonElement

interface SuspendedPropertyProvider {

    fun createBooleanProperty(
        key: String,
        initialValueProvider: () -> Boolean
    ): SuspendedProperty<Boolean>

    fun createIntProperty(
        key: String,
        initialValueProvider: () -> Int
    ): SuspendedProperty<Int>

    fun createLongProperty(
        key: String,
        initialValueProvider: () -> Long
    ): SuspendedProperty<Long>

    fun createStringProperty(
        key: String,
        initialValueProvider: () -> String
    ): SuspendedProperty<String>

    fun createNullableStringProperty(
        key: String,
        initialValueProvider: () -> String?
    ): SuspendedProperty<String?>

}

inline fun <reified T : Enum<T>> SuspendedPropertyProvider.createEnumProperty(
    key: String,
    crossinline initialValueProvider: () -> T
): SuspendedProperty<T> {
    return SuspendedPropertyProxy<T, String>(
        backingProperty = createStringProperty(
            key = key,
            initialValueProvider = { initialValueProvider().name }
        ),
        forwardConverter = { it.name },
        backwardConverter = { name ->
            enumValues<T>().find { it.name == name } ?: initialValueProvider()
        }
    )
}

fun SuspendedPropertyProvider.createLocalTimeProperty(
    key: String,
    initialValueProvider: () -> LocalTime
): SuspendedProperty<LocalTime> {
    return SuspendedPropertyProxy(
        backingProperty = createIntProperty(
            key = key,
            initialValueProvider = { initialValueProvider().toSecondOfDay() }
        ),
        forwardConverter = { it.toSecondOfDay() },
        backwardConverter = { LocalTime.fromSecondOfDay(it) }
    )
}

class SuspendedPropertyProxy<WrapperType, BackingType>(
    private val backingProperty: SuspendedProperty<BackingType>,
    private val forwardConverter: (WrapperType) -> BackingType,
    private val backwardConverter: (BackingType) -> WrapperType
) : SuspendedProperty<WrapperType> {

    override val key: String = backingProperty.key

    override suspend fun get(): WrapperType {
        return backwardConverter(backingProperty.get())
    }

    override suspend fun set(value: WrapperType) {
        backingProperty.set(forwardConverter(value))
    }

    override suspend fun isModified(): Boolean {
        return backingProperty.isModified()
    }

    override suspend fun backup(): JsonElement {
        return backingProperty.backup()
    }

    override suspend fun restore(value: JsonElement) {
        backingProperty.restore(value)
    }

}
