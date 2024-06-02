package ua.syt0r.kanji.core.user_data.practice

import ua.syt0r.kanji.core.user_data.practice.db.UserDataDatabaseManager

class SqlDelightVocabPracticeRepository(
    private val databaseManager: UserDataDatabaseManager
) : VocabPracticeRepository {

    override suspend fun createDeck(
        title: String,
        words: List<Long>
    ) = databaseManager.runTransaction {
        insertVocabDeck(title)
        val deckId = getLastInsertRowId().executeAsOne()
        words.forEach { insertVocabDeckEntry(it, deckId) }
    }

    override suspend fun deleteDeck(id: Long) = databaseManager.runTransaction {
        deleteVocabDeck(id)
    }

    override suspend fun getDecks(): List<VocabDeck> = databaseManager.runTransaction {
        getVocabDecks().executeAsList().map { VocabDeck(it.id, it.title, it.position) }
    }

    override suspend fun addWord(deckId: Long, wordId: Long) = databaseManager.runTransaction {
        insertVocabDeckEntry(wordId, deckId)
    }

    override suspend fun deleteWord(deckId: Long, wordId: Long) = databaseManager.runTransaction {
        deleteVocabDeckEntry(wordId, deckId)
    }

    override suspend fun getDeckWords(deckId: Long): List<Long> = databaseManager.runTransaction {
        getVocabDeckEntries(deckId).executeAsList().map { it.word_id }
    }

}

