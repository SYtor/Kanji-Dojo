package ua.syt0r.kanji.core.suspended_property

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

interface SuspendedProperty<T> {

    val key: String

    suspend fun get(): T
    suspend fun set(value: T)

    suspend fun isModified(): Boolean
    suspend fun backup(): JsonElement
    suspend fun restore(value: JsonElement)

}

interface BooleanSuspendedProperty : SuspendedProperty<Boolean> {

    override suspend fun backup(): JsonElement {
        return JsonPrimitive(get())
    }

    override suspend fun restore(value: JsonElement) {
        value as JsonPrimitive
        set(value.booleanOrNull ?: return)
    }

}

interface IntegerSuspendedProperty : SuspendedProperty<Int> {

    override suspend fun backup(): JsonElement {
        return JsonPrimitive(get())
    }

    override suspend fun restore(value: JsonElement) {
        value as JsonPrimitive
        set(value.content.toIntOrNull() ?: return)
    }

}

interface LongSuspendedProperty : SuspendedProperty<Long> {

    override suspend fun backup(): JsonElement {
        return JsonPrimitive(get())
    }

    override suspend fun restore(value: JsonElement) {
        value as JsonPrimitive
        set(value.content.toLongOrNull() ?: return)
    }

}

interface StringSuspendedProperty : SuspendedProperty<String> {

    override suspend fun backup(): JsonElement {
        return JsonPrimitive(get())
    }

    override suspend fun restore(value: JsonElement) {
        if (value !is JsonPrimitive) return
        set(value.content)
    }

}

interface NullableStringSuspendedProperty : SuspendedProperty<String?> {

    override suspend fun backup(): JsonElement {
        return JsonPrimitive(get())
    }

    override suspend fun restore(value: JsonElement) {
        if (value !is JsonPrimitive) return
        set(value.content)
    }

}