package ua.syt0r.kanji.core.srs

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate


data class SrsDecksData<Deck, PracticeType>(
    val decks: List<Deck>,
    val dailyLimitEnabled: Boolean,
    val dailyLimitConfiguration: DailyLimitConfiguration,
    val dailyProgress: SrsDailyProgress<PracticeType>
)

data class SrsDailyProgress<PracticeType>(
    val newReviewed: Int,
    val dueReviewed: Int,
    val leftoversMap: Map<PracticeType, DailyLeftover>
)

data class DailyLeftover(val new: Int, val due: Int)

interface SrsDeckData<PracticeType, ItemType> {
    val id: Long
    val title: String
    val position: Int
    val items: List<ItemType>
    val lastReview: Instant?
    val progressMap: Map<PracticeType, SrsDeckProgress<ItemType>>
}

data class SrsDeckProgress<ItemType>(
    val itemsData: Map<ItemType, SrsCardData>,
    val done: List<ItemType>,
    val due: List<ItemType>,
    val new: List<ItemType>,
    val dailyNew: List<ItemType>,
    val dailyDue: List<ItemType>
)

class PracticeTypeDeckData<ItemType>(
    val itemsData: Map<ItemType, SrsCardData>
)

data class SrsDeckDescriptor<ItemType, P : PracticeType>(
    val id: Long,
    val title: String,
    val position: Int,
    val lastReview: Instant?,
    val items: List<ItemType>,
    val itemsData: Map<P, PracticeTypeDeckData<ItemType>>
)

class DeckSortConfiguration(
    val sortByReviewDate: Boolean
)

data class SrsCardData(
    val key: SrsCardKey,
    val card: SrsCard?,
    val status: SrsItemStatus,
    val lapses: Int,
    val repeats: Int,
    val firstReview: Instant?,
    val firstReviewSrsDate: LocalDate?,
    val lastReview: Instant?,
    val lastReviewSrsDate: LocalDate?,
    val expectedReviewDate: LocalDate?
)

sealed interface DeckLimit {

    fun getLimit(practiceType: PracticeType): PracticeLimit

    object Disabled : DeckLimit {
        private val unlimitedLimit = PracticeLimit(Int.MAX_VALUE, Int.MAX_VALUE)
        override fun getLimit(practiceType: PracticeType) = unlimitedLimit
    }

    sealed interface EnabledDeckLimit : DeckLimit

    data class Combined(
        val limit: PracticeLimit,
        val newDone: Int,
        val dueDone: Int
    ) : EnabledDeckLimit {
        override fun getLimit(practiceType: PracticeType) = limit
    }

    data class Separate(
        val limitsMap: Map<out PracticeType, PracticeLimit>
    ) : EnabledDeckLimit {
        override fun getLimit(practiceType: PracticeType): PracticeLimit {
            return limitsMap[practiceType]!!
        }
    }

}