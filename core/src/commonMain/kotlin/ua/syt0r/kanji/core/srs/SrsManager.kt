package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.time.TimeUtils

abstract class SrsManager<ItemType, PracticeType, Deck>(
    deckChangesFlow: SharedFlow<Unit>,
    private val dailyLimitManager: DailyLimitManager,
    private val timeUtils: TimeUtils,
    coroutineScope: CoroutineScope
) where PracticeType : ua.syt0r.kanji.core.srs.PracticeType,
        Deck : SrsDeckData<PracticeType, ItemType> {

    private var cache: SrsDecksData<Deck, PracticeType>? = null

    private val srsCardComparator: Comparator<SrsCardData> =
        compareByDescending { (_, srsCardData) -> srsCardData?.lastReview }

    private val _dataChangeFlow = MutableSharedFlow<Unit>()
    val dataChangeFlow: SharedFlow<Unit> = _dataChangeFlow

    init {
        merge(deckChangesFlow, dailyLimitManager.changesFlow)
            .onEach {
                cache = null
                _dataChangeFlow.emit(Unit)
            }
            .launchIn(coroutineScope)
    }

    protected abstract val practiceTypes: List<PracticeType>

    protected abstract suspend fun getDeckDescriptors(): List<SrsDeckDescriptor<ItemType, PracticeType>>
    protected abstract suspend fun getDeckSortConfiguration(): DeckSortConfiguration
    protected abstract suspend fun getDeckLimit(
        configuration: DailyLimitConfiguration,
        newDoneToday: Int,
        dueDoneToday: Int
    ): DeckLimit.EnabledDeckLimit

    abstract fun createDeck(
        deckDescriptor: SrsDeckDescriptor<ItemType, PracticeType>,
        deckLimit: DeckLimit,
        currentSrsDate: LocalDate
    ): Deck

    private suspend fun getDecksComparator(): Comparator<SrsDeckDescriptor<ItemType, PracticeType>> {
        val deckSortConfiguration = getDeckSortConfiguration()
        return when {
            deckSortConfiguration.sortByReviewDate -> compareByDescending { it.lastReview }
            else -> compareByDescending { it.position }
        }
    }

    protected suspend fun getDecksInternal(): SrsDecksData<Deck, PracticeType> {
        cache?.let { return it }

        val deckDescriptors = getDeckDescriptors()
            .sortedWith(getDecksComparator())

        val cardsMap: Map<SrsCardKey, SrsCardData> = deckDescriptors
            .flatMap { deckDescriptor ->
                deckDescriptor.itemsData.flatMap { (_, data) ->
                    data.itemsData.map { (_, data) -> data }
                }
            }
            .associateBy { it.key }
            .toList()
            .sortedByDescending { (_, data) -> data.lastReview }
            .toMap()

        val currentSrsDate = timeUtils.now().toSrsDate()

        val cardsReviewedToday: Map<SrsCardKey, SrsCardData> = cardsMap
            .filter { (_, cardData) -> cardData.lastReview?.toSrsDate() == currentSrsDate }

        val newCardsReviewedToday: Map<SrsCardKey, SrsCardData> = cardsReviewedToday
            .filter { (_, cardData) -> cardData.firstReview?.toSrsDate() == currentSrsDate }

        val dueCardsReviewedToday: Map<SrsCardKey, SrsCardData> =
            cardsReviewedToday.minus(newCardsReviewedToday.keys)

        val isDailyLimitEnabled = dailyLimitManager.isEnabled()
        val dailyLimitConfiguration = dailyLimitManager.getConfiguration()

        val deckLimit = when {
            !isDailyLimitEnabled -> DeckLimit.Disabled
            else -> getDeckLimit(
                configuration = dailyLimitConfiguration,
                newDoneToday = newCardsReviewedToday.size,
                dueDoneToday = dueCardsReviewedToday.size
            )
        }

        val decks = deckDescriptors.map { createDeck(it, deckLimit, currentSrsDate) }

        val dailyProgress: SrsDailyProgress<PracticeType> = getDailyProgress(
            newCardsReviewedToday = newCardsReviewedToday,
            dueCardsReviewedToday = dueCardsReviewedToday,
            decks = decks,
            deckLimit = deckLimit
        )

        return SrsDecksData(
            decks = decks,
            dailyLimitEnabled = isDailyLimitEnabled,
            dailyLimitConfiguration = dailyLimitConfiguration,
            dailyProgress = dailyProgress
        ).also { cache = it }
    }

    protected fun Instant.toSrsDate(): LocalDate {
        return toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    protected fun getSrsStatus(srsCard: SrsCard?): SrsItemStatus {
        val expectedReviewTime = srsCard?.run { lastReview?.plus(interval) }
        val expectedReviewDate = expectedReviewTime?.toSrsDate()
        val currentDate = timeUtils.now().toSrsDate()
        return when {
            expectedReviewDate == null -> SrsItemStatus.New
            expectedReviewDate > currentDate -> SrsItemStatus.Done
            else -> SrsItemStatus.Review
        }
    }

    protected fun PracticeTypeDeckData<ItemType>.toProgress(
        deckLimit: DeckLimit,
        practiceType: PracticeType,
        today: LocalDate
    ): SrsDeckProgress<ItemType> {

        val done = mutableListOf<ItemType>()
        val due = mutableListOf<ItemType>()
        val new = mutableListOf<ItemType>()

        itemsData.forEach { (item, cardData) ->
            val list = when (cardData.status) {
                SrsItemStatus.Done -> done
                SrsItemStatus.Review -> due
                SrsItemStatus.New -> new
            }
            list.add(item)
        }

        val newToday: Int
        val dueToday: Int

        when (deckLimit) {
            is DeckLimit.Disabled -> {
                newToday = 0
                dueToday = 0
            }

            is DeckLimit.Combined -> {
                newToday = deckLimit.newDone
                dueToday = deckLimit.dueDone
            }

            is DeckLimit.Separate -> {
                val newTodayTmp = itemsData
                    .filter { it.value.firstReview?.toSrsDate() == today }
                newToday = newTodayTmp.size
                dueToday = itemsData
                    .filter { it.value.lastReview?.toSrsDate() == today }
                    .minus(newToday)
                    .size
            }
        }

        val practiceLimit = deckLimit.getLimit(practiceType)
        val newLeft = (practiceLimit.new - newToday).coerceAtLeast(0)
        val dueLeft = (practiceLimit.due - dueToday).coerceAtLeast(0)

        return SrsDeckProgress(
            itemsData = itemsData,
            done = done,
            due = due,
            new = new,
            dailyNew = new.take(newLeft),
            dailyDue = due.take(dueLeft)
        )
    }

    private fun getDailyProgress(
        newCardsReviewedToday: Map<SrsCardKey, SrsCardData>,
        dueCardsReviewedToday: Map<SrsCardKey, SrsCardData>,
        decks: List<Deck>,
        deckLimit: DeckLimit,
    ): SrsDailyProgress<PracticeType> {
        val leftoversByPracticeTypeMap = practiceTypes.associateWith { practiceType ->
            val limit = deckLimit.getLimit(practiceType)
            val newReviewedByPracticeType = newCardsReviewedToday
                .filter { it.key.practiceType == practiceType.srsPracticeType.value }
            val dueReviewedByPracticeType = dueCardsReviewedToday
                .filter { it.key.practiceType == practiceType.srsPracticeType.value }
            val newLeft = (limit.new - newReviewedByPracticeType.size).coerceAtLeast(0)
            val dueLeft = (limit.due - dueReviewedByPracticeType.size).coerceAtLeast(0)
            DailyLeftover(
                new = decks
                    .flatMap { deck -> deck.progressMap.getValue(practiceType).dailyNew }
                    .distinct()
                    .size
                    .coerceAtMost(newLeft),
                due = decks
                    .flatMap { deck -> deck.progressMap.getValue(practiceType).dailyDue }
                    .distinct()
                    .size
                    .coerceAtMost(dueLeft)
            )
        }

        val leftoversList = leftoversByPracticeTypeMap.toList()

        val totalLeftover = when (deckLimit) {
            is DeckLimit.Combined -> DailyLeftover(
                new = leftoversList.sumOf { it.second.new }.coerceAtMost(deckLimit.limit.new),
                due = leftoversList.sumOf { it.second.due }.coerceAtMost(deckLimit.limit.due)
            )

            DeckLimit.Disabled,
            is DeckLimit.Separate -> DailyLeftover(
                new = leftoversList.sumOf { (practiceType, leftover) ->
                    val limit = deckLimit.getLimit(practiceType).new
                    leftover.new.coerceAtMost(limit)
                },
                due = leftoversList.sumOf { (practiceType, leftover) ->
                    val limit = deckLimit.getLimit(practiceType).due
                    leftover.due.coerceAtMost(limit)
                }
            )
        }

        return SrsDailyProgress(
            newReviewed = newCardsReviewedToday.size,
            dueReviewed = dueCardsReviewedToday.size,
            leftoversByPracticeTypeMap = leftoversByPracticeTypeMap,
            totalLeftover = totalLeftover
        )
    }

}