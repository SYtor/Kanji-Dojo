package ua.syt0r.kanji.presentation.screen.main

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import ua.syt0r.kanji.core.launchWhenHasSubscribers

class DeepLinkHandler(
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
) {

    private val _deepLinksFlow = MutableSharedFlow<String>()
    val deepLinksFlow: SharedFlow<String> = _deepLinksFlow

    fun notifyDeepLink(link: String) {
        _deepLinksFlow.launchWhenHasSubscribers(coroutineScope) { _deepLinksFlow.emit(link) }
    }

}
