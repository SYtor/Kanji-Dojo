package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import androidx.compose.runtime.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import ua.syt0r.kanji.presentation.LifecycleState

interface LettersDashboardScreenContract {

    interface ViewModel {

        val state: State<ScreenState>

        fun updateDailyGoal(configuration: DailyGoalConfiguration)

        fun enablePracticeMergeMode()
        fun merge(data: LetterDecksMergeRequestData)

        fun enablePracticeReorderMode()
        fun reorder(data: LetterDecksReorderRequestData)

        fun enableDefaultMode()

        fun reportScreenShown()

    }

    sealed class ScreenState {

        object Loading : ScreenState()

        data class Loaded(
            val mode: StateFlow<LettersDashboardListMode>,
            val dailyIndicatorData: DailyIndicatorData
        ) : ScreenState()

    }

    interface LoadDataUseCase {
        fun load(
            lifecycleState: StateFlow<LifecycleState>
        ): Flow<RefreshableData<LettersDashboardScreenData>>
    }

    interface MergeDecksUseCase {
        suspend fun merge(data: LetterDecksMergeRequestData)
    }

    interface ApplySortUseCase {
        fun sort(
            sortByTime: Boolean,
            items: List<LettersDashboardItem>
        ): List<LettersDashboardItem>
    }

    interface UpdateSortUseCase {
        suspend fun update(data: LetterDecksReorderRequestData)
    }

}