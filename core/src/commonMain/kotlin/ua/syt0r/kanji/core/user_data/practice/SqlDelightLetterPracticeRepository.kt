package ua.syt0r.kanji.core.user_data.practice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import ua.syt0r.kanji.core.backup.BackupRestoreEventsProvider
import ua.syt0r.kanji.core.mergeSharedFlows
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.user_data.practice.db.UserDataDatabaseManager
import ua.syt0r.kanji.core.userdata.db.PracticeQueries

class SqlDelightLetterPracticeRepository(
    private val databaseManager: UserDataDatabaseManager,
    srsItemRepository: FsrsItemRepository,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) : LetterPracticeRepository {

    private val _changesFlow = MutableSharedFlow<Unit>()
    override val changesFlow: SharedFlow<Unit> = mergeSharedFlows(
        coroutineScope,
        _changesFlow,
        srsItemRepository.updatesFlow
    )

    private suspend fun <T> runTransaction(
        notifyDataChange: Boolean = false,
        block: PracticeQueries.() -> T,
    ): T {
        val result = databaseManager.runTransaction { block() }
        if (notifyDataChange) _changesFlow.emit(Unit)
        return result
    }

    override suspend fun createDeck(
        title: String,
        characters: List<String>,
    ) = runTransaction(notifyDataChange = true) {
        insertLetterDeck(name = title)

        val practiceId = getLastInsertRowId().executeAsOne()
        characters.forEach { insertOrIgnoreLetterDeckEntry(it, practiceId) }
    }

    override suspend fun createDeckAndMerge(
        title: String,
        deckIdToMerge: List<Long>,
    ) = runTransaction(notifyDataChange = true) {
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
    ) = runTransaction(notifyDataChange = true) {
        deckIdToPositionMap.forEach { (practiceId, position) ->
            updateLetterDeckPosition(position.toLong(), practiceId)
        }
    }

    override suspend fun deleteDeck(id: Long) = runTransaction(
        notifyDataChange = true
    ) {
        deleteLetterDeck(id)
    }

    override suspend fun updateDeck(
        id: Long,
        title: String,
        charactersToAdd: List<String>,
        charactersToRemove: List<String>,
    ) = runTransaction(notifyDataChange = true) {
        updateLetterDeckTitle(title, id)
        charactersToAdd.forEach { insertOrIgnoreLetterDeckEntry(it, id) }
        charactersToRemove.forEach { deleteLetterDeckEntry(id, it) }
    }

    override suspend fun getDecks(): List<Deck> = runTransaction {
        getAllLetterDecks().executeAsList().map {
            Deck(it.id, it.name, it.position.toInt())
        }
    }

    override suspend fun getDeck(
        id: Long,
    ): Deck = runTransaction {
        getLetterDeck(id).executeAsOne().run { Deck(id, name, position.toInt()) }
    }

    override suspend fun getDeckCharacters(
        id: Long,
    ): List<String> = runTransaction {
        getEntriesForLetterDeck(id).executeAsList().map { it.character }
    }

}

