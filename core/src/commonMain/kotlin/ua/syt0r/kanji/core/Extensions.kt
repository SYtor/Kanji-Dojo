package ua.syt0r.kanji.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.logger.Logger
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val DEFAULT_BUFFER_SIZE = 8192

@Throws(IOException::class)
fun InputStream.transferToCompat(out: OutputStream): Long {
    var transferred: Long = 0
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var read: Int
    while (read(buffer, 0, DEFAULT_BUFFER_SIZE).also { read = it } >= 0) {
        out.write(buffer, 0, read)
        transferred += read.toLong()
    }
    return transferred
}

fun <T> Flow<T>.debounceFirst(
    windowDuration: Duration = 500.milliseconds,
): Flow<T> {
    return flow {
        var lastEmitTime = Instant.DISTANT_PAST
        collect {
            val now = Clock.System.now()
            if (now.minus(lastEmitTime) > windowDuration) {
                emit(it)
                lastEmitTime = now
            } else {
                Logger.d("Ignoring quick emit")
            }
        }
    }
}

fun <T> T.runUnit(scope: T.() -> Unit) {
    scope()
}

fun CoroutineScope.launchUnit(block: suspend CoroutineScope.() -> Unit) {
    launch { block() }
}

fun <T> mergeSharedFlows(
    coroutineScope: CoroutineScope,
    vararg flows: Flow<T>,
): SharedFlow<T> {
    val sharedFlow = MutableSharedFlow<T>()
    coroutineScope.launch { merge(*flows).collect { sharedFlow.emit(it) } }
    return sharedFlow
}

fun MutableSharedFlow<*>.launchWhenHasSubscribers(
    coroutineScope: CoroutineScope,
    block: suspend () -> Unit
) {
    subscriptionCount.filter { it > 0 }
        .take(1)
        .onEach { block() }
        .launchIn(coroutineScope)
}
