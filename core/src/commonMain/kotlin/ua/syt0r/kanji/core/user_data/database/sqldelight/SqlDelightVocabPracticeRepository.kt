package ua.syt0r.kanji.core.user_data.database.sqldelight

import ua.syt0r.kanji.core.srs.VocabPracticeType
import ua.syt0r.kanji.core.user_data.database.ObservableRepository
import ua.syt0r.kanji.core.user_data.database.ObservableUserDataRepository
import ua.syt0r.kanji.core.user_data.database.SavedVocabCard
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseManager
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseTransactionLauncherScope
import ua.syt0r.kanji.core.user_data.database.VocabCardData
import ua.syt0r.kanji.core.user_data.database.VocabDeck
import ua.syt0r.kanji.core.user_data.database.VocabPracticeRepository
import ua.syt0r.kanji.core.userdata.db.UserDataQueries

class SqlDelightVocabPracticeRepository(
    observableRepository: ObservableUserDataRepository
) : VocabPracticeRepository,
    ObservableRepository by observableRepository,
    UserDataDatabaseTransactionLauncherScope by observableRepository {

    constructor(
        databaseManager: UserDataDatabaseManager
    ) : this(ObservableUserDataRepository(databaseManager))

    override suspend fun createDeck(
        title: String,
        words: List<VocabCardData>
    ) = writeTransaction {
        insertVocabDeck(title)
        val deckId = getLastInsertRowId().executeAsOne()
        words.forEach { insert(deckId, it) }
    }


    override suspend fun mergeDecks(
        newDeckTitle: String,
        deckIdToMerge: List<Long>
    ) = writeTransaction {
        insertVocabDeck(title = newDeckTitle)
        val deckId = getLastInsertRowId().executeAsOne()
        migrateVocabDeckEntries(deckId, deckIdToMerge)
        migrateDeckForReviewHistory(
            deckId = deckId,
            deckIdToMigrate = deckIdToMerge,
            practiceTypes = VocabPracticeType.srsPracticeTypeValues
        )
        deckIdToMerge.forEach { deleteVocabDeck(it) }
    }

    override suspend fun updateDeckPositions(
        deckIdToPositionMap: Map<Long, Int>
    ) = writeTransaction {
        deckIdToPositionMap.forEach { (deckId, position) ->
            updateVocabDeckPosition(position.toLong(), deckId)
        }
    }

    override suspend fun deleteDeck(id: Long) = writeTransaction { deleteVocabDeck(id) }

    override suspend fun getDecks(): List<VocabDeck> = readTransaction {
        getVocabDecks().executeAsList().map { VocabDeck(it.id, it.title, it.position.toInt()) }
    }

    override suspend fun updateDeck(
        id: Long,
        title: String,
        cardsToAdd: List<VocabCardData>,
        cardsToUpdate: List<SavedVocabCard>,
        cardsToRemove: List<Long>
    ) = writeTransaction {
        updateVocabDeckTitle(title, id)
        cardsToAdd.forEach { insert(id, it) }
        cardsToUpdate.forEach {
            updateVocabDeckEntry(
                it.data.kanjiReading,
                it.data.kanaReading,
                it.data.meaning,
                it.cardId
            )
        }
        cardsToRemove.forEach { deleteVocabDeckEntry(it) }
    }

    override suspend fun addCard(
        deckId: Long,
        data: VocabCardData
    ): Unit = writeTransaction {
        insert(deckId, data)
    }

    override suspend fun deleteCard(id: Long) = writeTransaction { deleteVocabDeckEntry(id = id) }

    override suspend fun getCardIdList(
        deckId: Long
    ): List<Long> = readTransaction {
        getVocabDeckEntryIds(deckId).executeAsList()
    }

    override suspend fun getAllCards(): List<SavedVocabCard> = readTransaction {
        getVocabDeckEntries().executeAsList().map {
            SavedVocabCard(
                cardId = it.id,
                deckId = it.deck_id,
                data = VocabCardData(
                    it.kanji_reading,
                    it.kana_reading,
                    it.meaning,
                    it.word_id!!
                )
            )
        }
    }

    private fun UserDataQueries.insert(
        deckId: Long,
        wordData: VocabCardData
    ) = wordData.apply {
        insertVocabDeckEntry(
            deckId,
            kanjiReading,
            kanaReading,
            meaning,
            dictionaryId
        )
    }

}

