package ua.syt0r.kanji.core.user_data.database

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import kotlin.time.Duration

interface ObservableRepository {
    val changesFlow: SharedFlow<Unit>
}

interface LetterPracticeRepository : ObservableRepository {
    suspend fun createDeck(title: String, characters: List<String>)
    suspend fun createDeckAndMerge(title: String, deckIdToMerge: List<Long>)
    suspend fun updateDeckPositions(deckIdToPositionMap: Map<Long, Int>)
    suspend fun deleteDeck(id: Long)
    suspend fun updateDeck(
        id: Long,
        title: String,
        charactersToAdd: List<String>,
        charactersToRemove: List<String>,
    )

    suspend fun getDecks(): List<LetterDeck>
    suspend fun getDeck(id: Long): LetterDeck
    suspend fun getDeckCharacters(id: Long): List<String>
}

interface VocabPracticeRepository : ObservableRepository {
    suspend fun createDeck(title: String, words: List<VocabCardData>)
    suspend fun mergeDecks(newDeckTitle: String, deckIdToMerge: List<Long>)
    suspend fun updateDeckPositions(deckIdToPositionMap: Map<Long, Int>)
    suspend fun deleteDeck(id: Long)
    suspend fun getDecks(): List<VocabDeck>

    suspend fun updateDeck(
        id: Long,
        title: String,
        cardsToAdd: List<VocabCardData>,
        cardsToRemove: List<Long>
    )

    suspend fun addCard(deckId: Long, data: VocabCardData)
    suspend fun deleteCard(id: Long)
    suspend fun getCardIdList(deckId: Long): List<Long>
    suspend fun getAllCards(): List<SavedVocabCard>
}

interface FsrsCardRepository : ObservableRepository {
    suspend fun get(key: SrsCardKey): FsrsCard?
    suspend fun getAll(): Map<SrsCardKey, FsrsCard>
    suspend fun update(key: SrsCardKey, card: FsrsCard)
}

interface ReviewHistoryRepository {
    suspend fun addReview(item: ReviewHistoryItem)
    suspend fun getReviews(start: Instant, end: Instant): List<ReviewHistoryItem>
    suspend fun getFirstReviewTime(key: String, practiceType: Long): Instant?
    suspend fun getDeckLastReview(deckId: Long, practiceTypes: List<Long>): Instant?
    suspend fun getTotalReviewsCount(): Long
    suspend fun getUniqueReviewItemsCount(practiceTypes: List<Long>): Long
    suspend fun getTotalPracticeTime(singleReviewDurationLimit: Long): Duration
    suspend fun getStreaks(): List<StreakData>
}