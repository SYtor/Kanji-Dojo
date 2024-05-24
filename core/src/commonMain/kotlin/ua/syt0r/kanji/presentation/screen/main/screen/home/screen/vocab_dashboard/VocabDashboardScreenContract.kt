package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import kotlinx.coroutines.flow.StateFlow

interface VocabDashboardScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun select(deck: VocabPracticeDeck)
        fun reportScreenShown()
    }

    sealed interface ScreenState {
        object NothingSelected : ScreenState
        data class DeckSelected(
            val deck: VocabPracticeDeck,
            val words: StateFlow<VocabPracticePreviewState>
        ) : ScreenState
    }

}