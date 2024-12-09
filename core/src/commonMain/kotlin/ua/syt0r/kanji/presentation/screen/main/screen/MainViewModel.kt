package ua.syt0r.kanji.presentation.screen.main.screen

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.sync.SyncConflictResolveStrategy
import ua.syt0r.kanji.core.sync.SyncManager
import ua.syt0r.kanji.core.sync.SyncFeatureState
import ua.syt0r.kanji.presentation.screen.main.MainContract

class MainViewModel(
    private val viewModelScope: CoroutineScope,
    private val syncManager: SyncManager
) : MainContract.ViewModel {

    override val syncFeatureState: StateFlow<SyncFeatureState> = syncManager.state

    override fun cancelSync() = syncManager.cancel()

    override fun resolveConflict(syncConflictResolveStrategy: SyncConflictResolveStrategy) {
        syncManager.resolveConflict(syncConflictResolveStrategy)
    }

}