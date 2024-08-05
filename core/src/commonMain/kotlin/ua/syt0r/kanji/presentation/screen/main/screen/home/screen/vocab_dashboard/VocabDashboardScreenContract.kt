package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksMergeRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksSortRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType

interface VocabDashboardScreenContract {

    interface ViewModel {

        val screenState: StateFlow<ScreenState>

        fun mergeDecks(data: DecksMergeRequestData)
        fun sortDecks(data: DecksSortRequestData)

    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Loaded(
            val listState: DeckDashboardListState,
            val srsPracticeType: State<VocabPracticeType>,
        ) : ScreenState

    }

}