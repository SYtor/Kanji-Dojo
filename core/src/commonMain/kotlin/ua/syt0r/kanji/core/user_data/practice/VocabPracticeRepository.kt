package ua.syt0r.kanji.core.user_data.practice

import kotlinx.coroutines.flow.SharedFlow

interface VocabPracticeRepository {

    val changesFlow: SharedFlow<Unit>

    suspend fun createDeck(title: String, words: List<Long>)
    suspend fun deleteDeck(id: Long)
    suspend fun getDecks(): List<VocabDeck>
    suspend fun updateDeck(
        id: Long,
        title: String,
        wordsToAdd: List<Long>,
        wordsToRemove: List<Long>
    )

    suspend fun addWord(deckId: Long, wordId: Long)
    suspend fun deleteWord(deckId: Long, wordId: Long)
    suspend fun getDeckWords(deckId: Long): List<Long>

}

data class VocabDeck(
    val id: Long,
    val title: String,
    val position: Long
)
