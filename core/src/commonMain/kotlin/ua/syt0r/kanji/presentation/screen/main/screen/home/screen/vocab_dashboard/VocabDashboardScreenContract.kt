package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType

interface VocabDashboardScreenContract {

    interface ViewModel {
        val screenState: StateFlow<ScreenState>
        val bottomSheetState: StateFlow<BottomSheetState>
        fun select(deck: DashboardVocabDeck)
        fun reportScreenShown()
    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Loaded(
            val srsPracticeType: State<VocabPracticeType>,
            val userDecks: List<DashboardVocabDeck>,
            val defaultDecks: List<DashboardVocabDeck>
        ) : ScreenState

    }

    sealed interface BottomSheetState {

        object Loading : BottomSheetState

        object Hidden : BottomSheetState

        data class DeckSelected(
            val deck: DashboardVocabDeck,
            val srsPracticeType: MutableState<VocabPracticeType>,
            val words: StateFlow<VocabPracticePreviewState>
        ) : BottomSheetState

    }

}