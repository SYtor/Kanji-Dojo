package ua.syt0r.kanji.presentation.screen.main.screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.sync.SyncManager
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.presentation.screen.main.MainContract

class MainViewModel(
    private val viewModelScope: CoroutineScope,
    private val syncManager: SyncManager
) : MainContract.ViewModel {

    override val syncState: StateFlow<SyncState> = syncManager.state
    override fun cancelSync() = syncManager.cancelSync()

}