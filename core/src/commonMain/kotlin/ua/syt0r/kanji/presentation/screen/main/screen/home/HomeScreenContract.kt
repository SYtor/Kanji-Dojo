package ua.syt0r.kanji.presentation.screen.main.screen.home

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.sync.SyncState

interface HomeScreenContract {

    interface ViewModel {
        val syncState: StateFlow<SyncState>
        fun sync()
    }

}