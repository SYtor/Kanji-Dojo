package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksMergeRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksSortRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.VocabDeckDashboardPracticeTypeItem

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
            val practiceTypeItems: List<VocabDeckDashboardPracticeTypeItem>,
            val selectedPracticeTypeItem: MutableState<VocabDeckDashboardPracticeTypeItem>,
        ) : ScreenState

    }

}