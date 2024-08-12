package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common

import androidx.compose.runtime.MutableState
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterPracticeType
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DisplayableEnum
import kotlin.time.Duration


interface DeckStudyType : DisplayableEnum

enum class LetterDeckStudyType(
    override val titleResolver: StringResolveScope<String>,
    val repoType: PreferencesLetterPracticeType
) : DeckStudyType {

    Writing(
        titleResolver = { lettersDashboard.itemWritingTitle },
        repoType = PreferencesLetterPracticeType.Writing
    ),
    Reading(
        titleResolver = { lettersDashboard.itemReadingTitle },
        repoType = PreferencesLetterPracticeType.Reading
    );

    companion object {
        fun from(practiceType: PreferencesLetterPracticeType): LetterDeckStudyType {
            return LetterDeckStudyType.values().first { it.repoType == practiceType }
        }
    }

}

data class DeckStudyProgress<T>(
    val all: List<T>,
    val known: List<T>,
    val review: List<T>,
    val new: List<T>,
    val quickLearn: List<T>,
    val quickReview: List<T>,
) {

    fun completionPercentage(): Float = when {
        all.isEmpty() -> 100f
        else -> (known.size + review.size).toFloat() / all.size * 100
    }

}

typealias LetterDeckStudyProgress = DeckStudyProgress<String>
typealias VocabDeckStudyProgress = DeckStudyProgress<Long>

interface DeckDashboardItem {
    val id: Long
    val title: String
    val position: Int
    val elapsedSinceLastReview: Duration?
    val studyProgress: Map<DeckStudyType, DeckStudyProgress<out Any>>
}

data class LetterDeckDashboardItem(
    override val id: Long,
    override val title: String,
    override val position: Int,
    override val elapsedSinceLastReview: Duration?,
    val writingProgress: LetterDeckStudyProgress,
    val readingProgress: LetterDeckStudyProgress
) : DeckDashboardItem {

    override val studyProgress: Map<DeckStudyType, LetterDeckStudyProgress> = mapOf(
        LetterDeckStudyType.Writing to writingProgress,
        LetterDeckStudyType.Reading to readingProgress
    )

}

data class VocabDeckDashboardItem(
    override val id: Long,
    override val title: String,
    override val position: Int,
    override val elapsedSinceLastReview: Duration?,
    override val studyProgress: Map<DeckStudyType, VocabDeckStudyProgress>,
) : DeckDashboardItem {

}

data class DeckDashboardListState(
    val items: List<DeckDashboardItem>,
    val appliedSortByReviewTime: MutableState<Boolean>,
    val mode: MutableState<DeckDashboardListMode>
)

sealed interface DeckDashboardListMode {

    object Browsing : DeckDashboardListMode

    data class MergeMode(
        val selected: MutableState<Set<Long>>,
        val title: MutableState<String>
    ) : DeckDashboardListMode

    data class SortMode(
        val reorderedList: MutableState<List<DeckDashboardItem>>,
        val sortByReviewTime: MutableState<Boolean>
    ) : DeckDashboardListMode

}

data class DecksMergeRequestData(
    val title: String,
    val deckIds: List<Long>
)

data class DecksSortRequestData(
    val reorderedList: List<DeckDashboardItem>,
    val sortByTime: Boolean
)