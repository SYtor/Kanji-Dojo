package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard

import androidx.compose.runtime.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration

interface PracticeDashboardScreenContract {

    interface ViewModel {

        val state: State<ScreenState>

        fun notifyScreenShown()
        fun updateDailyGoal(configuration: DailyGoalConfiguration)

        fun enablePracticeMergeMode()
        fun merge(data: PracticeMergeRequestData)

        fun enablePracticeReorderMode()
        fun reorder(data: PracticeReorderRequestData)

        fun enableDefaultMode()

        fun reportScreenShown()

    }

    sealed class ScreenState {

        object Loading : ScreenState()

        data class Loaded(
            val mode: StateFlow<PracticeDashboardListMode>,
            val dailyIndicatorData: DailyIndicatorData
        ) : ScreenState()

    }

    interface LoadDataUseCase {
        fun load(
            screenVisibilityEvents: Flow<Unit>,
            preferencesChangeEvents: Flow<Unit>
        ): Flow<RefreshableData<PracticeDashboardScreenData>>
    }

    interface MergePracticeSetsUseCase {
        suspend fun merge(data: PracticeMergeRequestData)
    }

    interface ApplySortUseCase {
        fun sort(
            sortByTime: Boolean,
            items: List<PracticeDashboardItem>
        ): List<PracticeDashboardItem>
    }

    interface UpdateSortUseCase {
        suspend fun update(data: PracticeReorderRequestData)
    }

}