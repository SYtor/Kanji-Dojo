package ua.syt0r.kanji.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import ua.syt0r.kanji.presentation.LifecycleState

sealed interface RefreshableData<T> {
    class Loading<T> : RefreshableData<T>
    data class Loaded<T>(val value: T) : RefreshableData<T>
}

fun <T> refreshableDataFlow(
    dataChangeFlow: SharedFlow<Unit>,
    lifecycleState: StateFlow<LifecycleState>,
    valueProvider: suspend () -> T
): Flow<RefreshableData<T>> = channelFlow {

    val waitForScreenVisibility = suspend {
        lifecycleState.filter { it == LifecycleState.Visible }.first()
    }

    dataChangeFlow.onStart { emit(Unit) }
        .collectLatest {
            send(RefreshableData.Loading())
            waitForScreenVisibility()
            send(RefreshableData.Loaded(valueProvider()))
        }

}.distinctUntilChanged { old, new ->
    if (old::class == new::class && old::class == RefreshableData.Loading::class) true
    else old == new
}