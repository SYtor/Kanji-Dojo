package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow

interface VocabDashboardScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun select(deck: VocabPracticeDeck)
        fun reportScreenShown()
    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Loaded(
            val userDecks: List<VocabPracticeDeck>,
            val defaultDecks: List<VocabPracticeDeck>,
            val deckSelectionState: State<VocabDeckSelectionState>
        ) : ScreenState

    }

}