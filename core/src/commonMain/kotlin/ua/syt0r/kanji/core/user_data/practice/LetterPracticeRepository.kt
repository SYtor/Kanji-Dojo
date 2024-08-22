package ua.syt0r.kanji.core.user_data.practice

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.LetterPracticeType

interface LetterPracticeRepository {

    val changesFlow: SharedFlow<Unit>

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

    suspend fun getDecks(): List<Deck>
    suspend fun getDeck(id: Long): Deck
    suspend fun getDeckCharacters(id: Long): List<String>

}

data class Deck(
    val id: Long,
    val name: String,
    val position: Int,
)

data class CharacterStudyProgress(
    val character: String,
    val practiceType: LetterPracticeType,
    val lastReviewTime: Instant,
    val repeats: Int,
    val lapses: Int,
)
