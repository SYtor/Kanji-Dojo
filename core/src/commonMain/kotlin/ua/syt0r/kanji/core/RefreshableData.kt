package ua.syt0r.kanji.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.zip

sealed interface RefreshableData<T> {
    class Loading<T> : RefreshableData<T>
    data class Loaded<T>(val value: T) : RefreshableData<T>
}

fun <T> refreshableDataFlow(
    dataChangeFlow: Flow<Unit>,
    invalidationRequestsFlow: Flow<Unit>,
    provider: suspend () -> T
): Flow<RefreshableData<T>> {
    return dataChangeFlow
        .onStart { emit(Unit) }
        .zip(invalidationRequestsFlow) { _, _ -> }
        .transform {
            emit(RefreshableData.Loading())
            emit(RefreshableData.Loaded(provider()))
        }
}