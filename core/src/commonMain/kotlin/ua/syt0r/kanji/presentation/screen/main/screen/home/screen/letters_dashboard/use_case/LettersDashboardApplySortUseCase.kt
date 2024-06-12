package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case

import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract
import kotlin.time.Duration

class LettersDashboardApplySortUseCase : LettersDashboardScreenContract.ApplySortUseCase {

    override fun sort(
        sortByTime: Boolean,
        items: List<LettersDashboardItem>
    ): List<LettersDashboardItem> {
        return when {
            sortByTime -> items.sortedBy { it.timeSinceLastReview ?: Duration.INFINITE }
            else -> items.sortedByDescending { it.position }
        }
    }

}