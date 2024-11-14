package ua.syt0r.kanji.presentation.screen.main

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.sync.SyncConflictResolveStrategy
import ua.syt0r.kanji.core.sync.SyncState

interface MainContract {

    interface ViewModel {
        val syncState: StateFlow<SyncState>
        fun cancelSync()
        fun resolveConflict(syncConflictResolveStrategy: SyncConflictResolveStrategy)
    }

}