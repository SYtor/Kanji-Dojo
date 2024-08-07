package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import androidx.compose.runtime.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.presentation.LifecycleState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksMergeRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksSortRequestData

interface LettersDashboardScreenContract {

    interface ViewModel {

        val state: State<ScreenState>

        fun mergeDecks(data: DecksMergeRequestData)
        fun sortDecks(data: DecksSortRequestData)

        fun reportScreenShown()

    }

    sealed class ScreenState {

        object Loading : ScreenState()

        data class Loaded(
            val listState: DeckDashboardListState,
            val dailyIndicatorData: DailyIndicatorData
        ) : ScreenState()

    }

    interface LoadDataUseCase {
        fun load(
            lifecycleState: StateFlow<LifecycleState>
        ): Flow<RefreshableData<LettersDashboardScreenData>>
    }

    interface MergeDecksUseCase {
        suspend operator fun invoke(data: DecksMergeRequestData)
    }

    interface UpdateSortUseCase {
        suspend fun update(data: DecksSortRequestData)
    }

}