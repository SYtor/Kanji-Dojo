package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import kotlinx.coroutines.flow.StateFlow

interface VocabDashboardScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun select(set: VocabPracticeSet)
        fun reportScreenShown()
    }

    sealed interface ScreenState {
        object NothingSelected : ScreenState
        data class SelectedSet(
            val set: VocabPracticeSet,
            val words: StateFlow<VocabPracticePreviewState>
        ) : ScreenState
    }

}