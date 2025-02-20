package ua.syt0r.kanji.presentation.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.logger.Logger

interface Paginateable<T> {
    val total: Int
    val list: StateFlow<List<T>>
    val canLoadMore: StateFlow<Boolean>
    fun loadMore()
}

data class PaginateableState<T>(
    val total: Int,
    private val listState: State<List<T>>,
    private val canLoadMoreState: State<Boolean>
) {
    val list: List<T> by listState
    val canLoadMore: Boolean by canLoadMoreState
}

@Composable
fun <T> Paginateable<T>.collectAsState(): PaginateableState<T> {
    val listState = list.collectAsState()
    val isLoadingState = canLoadMore.collectAsState()
    return remember { PaginateableState(total, listState, isLoadingState) }
}

@Composable
fun PaginationLoadLaunchedEffect(
    listState: LazyListState,
    prefetchDistance: Int = 50,
    loadMore: () -> Unit
) {
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { it.isNearListEnd(prefetchDistance) }
            .filter { it }
            .collect { loadMore() }
    }
}

suspend fun <T> paginateable(
    coroutineScope: CoroutineScope,
    limit: Int,
    load: suspend (offset: Int) -> List<T>
): Paginateable<T> {

    var offset = 0
    val loadMoreRequestsChannel = Channel<Unit>(onBufferOverflow = BufferOverflow.DROP_LATEST)

    val list = MutableStateFlow<List<T>>(emptyList())

    val canLoadMoreState: StateFlow<Boolean> = list
        .map { it.size < limit }
        .stateIn(coroutineScope)

    coroutineScope.launch(Dispatchers.IO) {
        loadMoreRequestsChannel.consumeAsFlow()
            .combineTransform(canLoadMoreState) { _, canLoadMore ->
                if (canLoadMore) emit(Unit)
            }
            .collect {
                Logger.d("loading more data")
                val extraData = load(offset)
                offset += extraData.size
                list.value = list.value.plus(extraData)
            }
    }

    return object : Paginateable<T> {
        override val total: Int = limit
        override val list: StateFlow<List<T>> = list
        override val canLoadMore: StateFlow<Boolean> = canLoadMoreState

        override fun loadMore() {
            loadMoreRequestsChannel.trySend(Unit)
        }
    }

}