package ua.syt0r.kanji.core.user_data.database.sqldelight

import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.user_data.database.LetterDeck
import ua.syt0r.kanji.core.user_data.database.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.database.ObservableUserDataRepository
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseManager

class SqlDelightLetterPracticeRepository(
    databaseManager: UserDataDatabaseManager,
) : ObservableUserDataRepository(databaseManager), LetterPracticeRepository {

    override suspend fun createDeck(
        title: String,
        characters: List<String>,
    ) = writeTransaction {
        insertLetterDeck(name = title)

        val practiceId = getLastInsertRowId().executeAsOne()
        characters.forEach { insertOrIgnoreLetterDeckEntry(it, practiceId) }
    }

    override suspend fun createDeckAndMerge(
        title: String,
        deckIdToMerge: List<Long>,
    ) = writeTransaction {
        insertLetterDeck(name = title)
        val deckId = getLastInsertRowId().executeAsOne()

        migrateLetterDeckEntries(deckId, deckIdToMerge)
        migrateDeckForReviewHistory(
            deckId = deckId,
            deckIdToMigrate = deckIdToMerge,
            practiceTypes = LetterPracticeType.srsPracticeTypeValues
        )

        deleteLetterDecks(deckIdToMerge)
    }

    override suspend fun updateDeckPositions(
        deckIdToPositionMap: Map<Long, Int>,
    ) = writeTransaction {
        deckIdToPositionMap.forEach { (practiceId, position) ->
            updateLetterDeckPosition(position.toLong(), practiceId)
        }
    }

    override suspend fun deleteDeck(id: Long) = writeTransaction { deleteLetterDeck(id) }

    override suspend fun updateDeck(
        id: Long,
        title: String,
        charactersToAdd: List<String>,
        charactersToRemove: List<String>,
    ) = writeTransaction {
        updateLetterDeckTitle(title, id)
        charactersToAdd.forEach { insertOrIgnoreLetterDeckEntry(it, id) }
        charactersToRemove.forEach { deleteLetterDeckEntry(id, it) }
    }

    override suspend fun getDecks(): List<LetterDeck> = readTransaction {
        getAllLetterDecks().executeAsList().map {
            LetterDeck(it.id, it.name, it.position.toInt())
        }
    }

    override suspend fun getDeck(
        id: Long,
    ): LetterDeck = readTransaction {
        getLetterDeck(id).executeAsOne().run { LetterDeck(id, name, position.toInt()) }
    }

    override suspend fun getDeckCharacters(
        id: Long,
    ): List<String> = readTransaction {
        getEntriesForLetterDeck(id).executeAsList().map { it.character }
    }

}

