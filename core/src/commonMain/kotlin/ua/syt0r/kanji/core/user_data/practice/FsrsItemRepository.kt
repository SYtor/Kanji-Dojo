package ua.syt0r.kanji.core.user_data.practice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.backup.BackupRestoreEventsProvider
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus
import ua.syt0r.kanji.core.user_data.practice.db.UserDataDatabaseManager
import ua.syt0r.kanji.core.userdata.db.Fsrs_card
import kotlin.time.Duration.Companion.milliseconds

interface FsrsItemRepository {
    val updatesFlow: SharedFlow<Unit>

    suspend fun get(key: SrsCardKey): FsrsCard?
    suspend fun getAll(): Map<SrsCardKey, FsrsCard>
    suspend fun update(key: SrsCardKey, card: FsrsCard)
}

class SqlDelightFsrsItemRepository(
    private val userDataDatabaseManager: UserDataDatabaseManager,
    backupRestoreEventsProvider: BackupRestoreEventsProvider,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : FsrsItemRepository {

    private class RepoData(
        val cardsMap: MutableMap<SrsCardKey, FsrsCard>
    )

    private val repoDataState = MutableStateFlow(asyncRepoData())

    private val _updatesFlow = MutableSharedFlow<Unit>()
    override val updatesFlow: SharedFlow<Unit> = _updatesFlow

    init {
        backupRestoreEventsProvider.onRestoreEventsFlow
            .onEach {
                resetRepoData()
                _updatesFlow.emit(Unit)
            }
            .launchIn(coroutineScope)
    }

    override suspend fun get(key: SrsCardKey): FsrsCard? {
        return repoDataState.value.await().cardsMap[key]
    }

    override suspend fun getAll(): Map<SrsCardKey, FsrsCard> {
        return repoDataState.value.await().cardsMap
    }

    override suspend fun update(key: SrsCardKey, card: FsrsCard) {
        val repoData = repoDataState.value.await()
        repoData.cardsMap[key] = card
        userDataDatabaseManager.runTransaction(true) { upsertFsrsCard(covert(key, card)) }
        _updatesFlow.emit(Unit)
    }

    private fun asyncRepoData(): Deferred<RepoData> {
        return coroutineScope.async(start = CoroutineStart.LAZY) {
            userDataDatabaseManager.runTransaction(false) {
                val cardsMap = getFsrsCards().executeAsList()
                    .associate { SrsCardKey(it.key, it.practice_type) to it.convert() }
                    .toMutableMap()
                RepoData(cardsMap)
            }
        }
    }

    private fun resetRepoData() {
        repoDataState.value = asyncRepoData()
    }

    private val dbValueToSrcCardStatus: Map<Int, FsrsCardStatus> = FsrsCardStatus.entries
        .associateBy { it.ordinal }

    private fun covert(key: SrsCardKey, card: FsrsCard): Fsrs_card {
        card.params as FsrsCardParams.Existing
        return Fsrs_card(
            key = key.itemKey,
            practice_type = key.practiceType,
            status = card.status.ordinal.toLong(),
            stability = card.params.stability,
            difficulty = card.params.difficulty,
            lapses = card.lapses.toLong(),
            repeats = card.repeats.toLong(),
            last_review = card.lastReview!!.toEpochMilliseconds(),
            interval = card.interval.inWholeMilliseconds
        )
    }

    private fun Fsrs_card.convert(): FsrsCard = FsrsCard(
        params = FsrsCardParams.Existing(
            difficulty = difficulty,
            stability = stability,
            reviewTime = Instant.fromEpochMilliseconds(last_review)
        ),
        status = dbValueToSrcCardStatus.getValue(status.toInt()),
        interval = interval.milliseconds,
        lapses = lapses.toInt(),
        repeats = repeats.toInt()
    )

}