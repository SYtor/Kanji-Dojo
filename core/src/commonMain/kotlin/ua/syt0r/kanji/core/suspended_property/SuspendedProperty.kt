package ua.syt0r.kanji.core.suspended_property

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.json.JsonPrimitive

interface SuspendedProperty<T> {

    val key: String
    val onModified: SharedFlow<T>

    suspend fun get(): T
    suspend fun set(value: T)

    suspend fun isModified(): Boolean

    suspend fun backup(): JsonPrimitive?
    suspend fun restore(value: JsonPrimitive)

}
