package ua.syt0r.kanji.presentation.screen.main.screen.home

interface HomeScreenContract {

    interface ViewModel {
        val syncIconState: SyncIconState
        fun sync()
    }

}