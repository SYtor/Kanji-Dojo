package ua.syt0r.kanji.presentation.screen.main

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class DeepLinkHandler(
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
) {

    private val _deepLinksFlow = MutableSharedFlow<String>()
    val deepLinksFlow: SharedFlow<String> = _deepLinksFlow

    fun notifyDeepLink(link: String) {
        coroutineScope.launch {
            waitForHandlerSubscribers()
            _deepLinksFlow.emit(link)
        }
    }

    private suspend fun waitForHandlerSubscribers() {
        _deepLinksFlow.subscriptionCount.filter { it > 0 }.take(1).collect()
    }

}
