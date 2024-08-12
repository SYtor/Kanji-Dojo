package ua.syt0r.kanji.core.suspended_property

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

/***
 * Component for repositories with following features:
 * - multiplatform provider - need to maintain only one repository
 * - suspend invocations - allows to avoid getters and setters since Kotlin properties can't be suspended
 * - exposes json values for backup support
 */
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

interface StringSuspendedProperty : SuspendedProperty<String> {

    override suspend fun backup(): JsonElement {
        return JsonPrimitive(get())
    }

    override suspend fun restore(value: JsonElement) {
        if (value !is JsonPrimitive) return
        set(value.content)
    }

}