package ua.syt0r.kanji.core.user_data.database.sqldelight

import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus
import ua.syt0r.kanji.core.user_data.database.CachedUserDataState
import ua.syt0r.kanji.core.user_data.database.FsrsCardRepository
import ua.syt0r.kanji.core.user_data.database.ObservableRepository
import ua.syt0r.kanji.core.user_data.database.ObservableUserDataRepository
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseManager
import ua.syt0r.kanji.core.user_data.database.UserDataDatabaseTransactionLauncherScope
import ua.syt0r.kanji.core.userdata.db.Fsrs_card
import ua.syt0r.kanji.core.userdata.db.UserDataQueries
import kotlin.time.Duration.Companion.milliseconds

class SqlDelightFsrsCardRepository private constructor(
    observableRepository: ObservableUserDataRepository,
    private val cachedUserDataState: CachedUserDataState<RepoData>
) : FsrsCardRepository,
    ObservableRepository by observableRepository,
    UserDataDatabaseTransactionLauncherScope by observableRepository {

    constructor(
        userDataDatabaseManager: UserDataDatabaseManager
    ) : this(
        ObservableUserDataRepository(userDataDatabaseManager),
        CachedUserDataState(
            resetFlow = userDataDatabaseManager.databaseChangeEvents,
            databaseManager = userDataDatabaseManager,
            provider = { loadRepoDataFromDB() }
        )
    )

    override suspend fun get(key: SrsCardKey): FsrsCard? {
        return getRepoData().cardsMap[key]
    }

    override suspend fun getAll(): Map<SrsCardKey, FsrsCard> {
        return getRepoData().cardsMap
    }

    override suspend fun update(key: SrsCardKey, card: FsrsCard) {
        val repoData = getRepoData()
        repoData.cardsMap[key] = card
        writeTransaction { upsertFsrsCard(covert(key, card)) }
    }

    private suspend fun getRepoData(): RepoData {
        return cachedUserDataState.data.value.await()
    }

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

    private class RepoData(
        val cardsMap: MutableMap<SrsCardKey, FsrsCard>
    )

    companion object {

        private val dbValueToSrcCardStatus: Map<Int, FsrsCardStatus> = FsrsCardStatus.entries
            .associateBy { it.ordinal }

        private fun UserDataQueries.loadRepoDataFromDB(): RepoData {
            val cardsMap = getFsrsCards().executeAsList()
                .associate { SrsCardKey(it.key, it.practice_type) to it.convert() }
                .toMutableMap()
            return RepoData(cardsMap)
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

}