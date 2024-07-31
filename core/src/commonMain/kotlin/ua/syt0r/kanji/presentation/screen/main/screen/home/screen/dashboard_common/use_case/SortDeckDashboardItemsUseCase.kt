package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.use_case

import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardItem
import kotlin.time.Duration

interface SortDeckDashboardItemsUseCase {
    operator fun invoke(
        sortByTime: Boolean,
        items: List<DeckDashboardItem>
    ): List<DeckDashboardItem>
}

class DefaultSortDeckDashboardItemsUseCase : SortDeckDashboardItemsUseCase {

    override operator fun invoke(
        sortByTime: Boolean,
        items: List<DeckDashboardItem>
    ): List<DeckDashboardItem> {
        return when {
            sortByTime -> items.sortedBy { it.elapsedSinceLastReview ?: Duration.INFINITE }
            else -> items.sortedByDescending { it.position }
        }
    }

}