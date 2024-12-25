package ua.syt0r.kanji.presentation.screen.main.screen.home

interface HomeScreenContract {

    interface ViewModel {
        val defaultTab: HomeScreenTab
        val syncIconState: SyncIconState
        fun trySync(): Boolean
    }

}