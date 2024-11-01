package  ua.syt0r.kanji.presentation.screen.main.screen.home

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.sync.SyncManager
import ua.syt0r.kanji.core.sync.SyncState

class HomeViewModel(
    private val viewModelScope: CoroutineScope,
    private val syncManager: SyncManager
) : HomeScreenContract.ViewModel {

    override val syncState: StateFlow<SyncState> = syncManager.state
    override fun sync() = syncManager.forceSync()

}