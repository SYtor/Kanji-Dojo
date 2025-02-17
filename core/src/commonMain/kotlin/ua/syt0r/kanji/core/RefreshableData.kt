package ua.syt0r.kanji.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.presentation.LifecycleState
import kotlin.time.measureTime

sealed interface RefreshableData<T> {
    class Loading<T> : RefreshableData<T>
    data class Loaded<T>(val value: T) : RefreshableData<T>
}

inline fun <reified T> refreshableDataFlow(
    dataChangeFlow: Flow<Unit>,
    lifecycleState: StateFlow<LifecycleState>,
    noinline valueProvider: suspend CoroutineScope.() -> T
): Flow<RefreshableData<T>> = channelFlow {

    val waitForScreenVisibility = suspend {
        lifecycleState.filter { it == LifecycleState.Visible }.first()
    }

    dataChangeFlow.onStart { emit(Unit) }
        .collectLatest {
            send(RefreshableData.Loading())
            waitForScreenVisibility()

            val value: T
            val loadingTime = measureTime { value = valueProvider.invoke(this) }
            Logger.d("Loaded ${T::class.qualifiedName} data, loadingTime[$loadingTime]")

            send(RefreshableData.Loaded(value))
        }

}.distinctUntilChanged { old, new ->
    if (old::class == new::class && old::class == RefreshableData.Loading::class) true
    else old == new
}