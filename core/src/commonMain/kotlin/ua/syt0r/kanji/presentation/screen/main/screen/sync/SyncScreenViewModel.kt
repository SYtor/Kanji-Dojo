package ua.syt0r.kanji.presentation.screen.main.screen.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.presentation.screen.main.screen.sync.SyncScreenContract.ScreenState


class SyncScreenViewModel(
    coroutineScope: CoroutineScope
) : SyncScreenContract.ViewModel {

    private val _state: StateFlow<ScreenState> = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

}