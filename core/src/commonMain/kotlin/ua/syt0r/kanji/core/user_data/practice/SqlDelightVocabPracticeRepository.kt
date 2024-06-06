package ua.syt0r.kanji.core.user_data.practice

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import ua.syt0r.kanji.core.user_data.practice.db.UserDataDatabaseManager
import ua.syt0r.kanji.core.userdata.db.PracticeQueries

class SqlDelightVocabPracticeRepository(
    private val databaseManager: UserDataDatabaseManager
) : VocabPracticeRepository {

    private val _changesFlow = MutableSharedFlow<Unit>()
    override val changesFlow: SharedFlow<Unit> = _changesFlow

    private suspend fun <T> UserDataDatabaseManager.runModifyingTransaction(
        block: PracticeQueries.() -> T
    ): T {
        val result = runTransaction(block = block)
        _changesFlow.emit(Unit)
        return result
    }

    override suspend fun createDeck(
        title: String,
        words: List<Long>
    ) = databaseManager.runModifyingTransaction {
        insertVocabDeck(title)
        val deckId = getLastInsertRowId().executeAsOne()
        words.forEach { insertVocabDeckEntry(it, deckId) }
    }

    override suspend fun deleteDeck(id: Long) = databaseManager.runModifyingTransaction {
        deleteVocabDeck(id)
    }

    override suspend fun getDecks(): List<VocabDeck> = databaseManager.runTransaction {
        getVocabDecks().executeAsList().map { VocabDeck(it.id, it.title, it.position) }
    }

    override suspend fun updateDeck(
        id: Long,
        title: String,
        wordsToAdd: List<Long>,
        wordsToRemove: List<Long>
    ) = databaseManager.runModifyingTransaction {
        updateVocabDeckTitle(title, id)
        wordsToAdd.forEach { insertVocabDeckEntry(it, id) }
        wordsToRemove.forEach { deleteVocabDeckEntry(it, id) }
    }

    override suspend fun addWord(
        deckId: Long,
        wordId: Long
    ) = databaseManager.runModifyingTransaction {
        insertVocabDeckEntry(wordId, deckId)
    }

    override suspend fun deleteWord(
        deckId: Long,
        wordId: Long
    ) = databaseManager.runModifyingTransaction {
        deleteVocabDeckEntry(wordId, deckId)
    }

    override suspend fun getDeckWords(deckId: Long): List<Long> = databaseManager.runTransaction {
        getVocabDeckEntries(deckId).executeAsList().map { it.word_id }
    }

}

