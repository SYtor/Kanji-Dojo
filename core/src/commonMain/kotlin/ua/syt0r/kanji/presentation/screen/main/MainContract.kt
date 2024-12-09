package ua.syt0r.kanji.presentation.screen.main

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.sync.SyncConflictResolveStrategy
import ua.syt0r.kanji.core.sync.SyncFeatureState

interface MainContract {

    interface ViewModel {
        val syncFeatureState: StateFlow<SyncFeatureState>
        fun cancelSync()
        fun resolveConflict(syncConflictResolveStrategy: SyncConflictResolveStrategy)
    }

}