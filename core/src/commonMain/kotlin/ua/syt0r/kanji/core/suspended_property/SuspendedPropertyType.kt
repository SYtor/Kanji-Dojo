package ua.syt0r.kanji.core.suspended_property

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import kotlinx.serialization.serializer

sealed interface SuspendedPropertyType<ExposedType, BackingType> {

    fun createKey(key: String): Preferences.Key<BackingType>

    fun convertToBacking(value: ExposedType): BackingType
    fun convertToExposed(value: BackingType): ExposedType

    fun backup(value: ExposedType): JsonPrimitive
    fun restore(value: JsonPrimitive): ExposedType

    interface Raw<T> : SuspendedPropertyType<T, T> {
        override fun convertToBacking(value: T): T = value
        override fun convertToExposed(value: T): T = value
    }

    interface Wrapper<ExposedType, BackingType> : SuspendedPropertyType<ExposedType, BackingType> {

        val backingPropertyType: Raw<BackingType>

        override fun createKey(
            key: String
        ): Preferences.Key<BackingType> = backingPropertyType.createKey(key)

        override fun backup(value: ExposedType): JsonPrimitive =
            backingPropertyType.backup(convertToBacking(value))

        override fun restore(value: JsonPrimitive): ExposedType =
            convertToExposed(backingPropertyType.restore(value))

    }

}

object BooleanSuspendedPropertyType : SuspendedPropertyType.Raw<Boolean> {
    override fun createKey(key: String) = booleanPreferencesKey(key)
    override fun backup(value: Boolean): JsonPrimitive = JsonPrimitive(value)
    override fun restore(value: JsonPrimitive): Boolean = value.boolean
}

object IntSuspendedPropertyType : SuspendedPropertyType.Raw<Int> {
    override fun createKey(key: String) = intPreferencesKey(key)
    override fun backup(value: Int): JsonPrimitive = JsonPrimitive(value)
    override fun restore(value: JsonPrimitive): Int = value.int
}

object LongSuspendedPropertyType : SuspendedPropertyType.Raw<Long> {
    override fun createKey(key: String) = longPreferencesKey(key)
    override fun backup(value: Long): JsonPrimitive = JsonPrimitive(value)
    override fun restore(value: JsonPrimitive): Long = value.long
}

object StringSuspendedPropertyType : SuspendedPropertyType.Raw<String> {
    override fun createKey(key: String) = stringPreferencesKey(key)
    override fun backup(value: String): JsonPrimitive = JsonPrimitive(value)
    override fun restore(value: JsonPrimitive): String = value.content
}


class EnumSuspendedPropertyType<T : Enum<T>>(
    private val enumValues: Array<T>
) : SuspendedPropertyType.Wrapper<T, String> {

    override val backingPropertyType = StringSuspendedPropertyType

    override fun convertToBacking(value: T): String = value.name
    override fun convertToExposed(value: String): T = enumValues.first { it.name == value }

    companion object {
        inline fun <reified T : Enum<T>> enumSuspendedPropertyType(): EnumSuspendedPropertyType<T> {
            return EnumSuspendedPropertyType(enumValues<T>())
        }
    }

}

object LocalTimeSuspendedPropertyType : SuspendedPropertyType.Wrapper<LocalTime, Int> {
    override val backingPropertyType = IntSuspendedPropertyType
    override fun convertToBacking(value: LocalTime): Int = value.toSecondOfDay()
    override fun convertToExposed(value: Int): LocalTime = LocalTime.fromSecondOfDay(value)
}

object InstantSuspendedPropertyType : SuspendedPropertyType.Wrapper<Instant, Long> {
    override val backingPropertyType: SuspendedPropertyType.Raw<Long> = LongSuspendedPropertyType
    override fun convertToBacking(value: Instant): Long = value.toEpochMilliseconds()
    override fun convertToExposed(value: Long): Instant = Instant.fromEpochMilliseconds(value)
}


inline fun <reified T> jsonPojoSuspendedPropertyType(): JsonPojoSuspendedPropertyType<T> {
    val json = Json.Default
    return JsonPojoSuspendedPropertyType(
        serializer = json.serializersModule.serializer<T>(),
        json = json
    )
}


class JsonPojoSuspendedPropertyType<T>(
    private val serializer: KSerializer<T>,
    private val json: Json
) : SuspendedPropertyType.Wrapper<T, String> {
    override val backingPropertyType: SuspendedPropertyType.Raw<String> =
        StringSuspendedPropertyType

    override fun convertToBacking(value: T): String = json.encodeToString(serializer, value)
    override fun convertToExposed(value: String): T = json.decodeFromString(serializer, value)
}

