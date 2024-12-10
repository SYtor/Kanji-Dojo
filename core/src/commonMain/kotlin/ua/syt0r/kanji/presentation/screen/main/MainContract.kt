package ua.syt0r.kanji.presentation.screen.main

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.sync.SyncConflictResolveStrategy

interface MainContract {

    interface ViewModel {
        val syncDialogState: StateFlow<SyncDialogState>
        fun cancelSync()
        fun resolveConflict(syncConflictResolveStrategy: SyncConflictResolveStrategy)
    }

}