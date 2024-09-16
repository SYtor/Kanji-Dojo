package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository

interface VocabSrsManager {
    val dataChangeFlow: SharedFlow<Unit>
    suspend fun getDecks(): VocabSrsDecksData
    suspend fun getDeck(deckId: Long): VocabSrsDeck
}

typealias VocabSrsDecksData = SrsDecksData<VocabSrsDeck, VocabPracticeType>
typealias VocabSrsDeckDescriptor = SrsDeckDescriptor<Long, VocabPracticeType>
typealias VocabSrsDeckProgress = SrsDeckProgress<Long>

data class VocabSrsDeck(
    override val id: Long,
    override val title: String,
    override val position: Int,
    override val items: List<Long>,
    override val lastReview: Instant?,
    override val progressMap: Map<VocabPracticeType, VocabSrsDeckProgress>
) : SrsDeckData<VocabPracticeType, Long>

class DefaultVocabSrsManager(
    private val practiceRepository: VocabPracticeRepository,
    private val srsItemRepository: SrsItemRepository,
    dailyLimitManager: DailyLimitManager,
    timeUtils: TimeUtils,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    coroutineScope: CoroutineScope
) : SrsManager<Long, VocabPracticeType, VocabSrsDeck>(
    deckChangesFlow = practiceRepository.changesFlow,
    dailyLimitManager = dailyLimitManager,
    timeUtils = timeUtils,
    coroutineScope = coroutineScope
), VocabSrsManager {

    override val practiceTypes: List<VocabPracticeType> = VocabPracticeType.values().toList()

    override suspend fun getDecks(): VocabSrsDecksData {
        return getDecksInternal()
    }

    override suspend fun getDeck(deckId: Long): VocabSrsDeck {
        return getDecksInternal().decks.first { it.id == deckId }
    }

    override suspend fun getDeckDescriptors(): List<VocabSrsDeckDescriptor> {
        return practiceRepository.getDecks().map {
            val items = practiceRepository.getDeckWords(it.id)

            val itemsDataMap = VocabPracticeType.values().associateWith { practiceType ->
                val itemsData: Map<Long, SrsCardData> = items.associateWith { wordId ->
                    val key = practiceType.toSrsKey(wordId)
                    val card = srsItemRepository.get(key)
                    val firstReview = reviewHistoryRepository.getFirstReviewTime(
                        key = key.itemKey,
                        practiceType = practiceType.srsPracticeType.value
                    )
                    SrsCardData(
                        key = key,
                        card = card,
                        status = getSrsStatus(card),
                        lapses = card?.fsrsCard?.lapses ?: 0,
                        repeats = card?.fsrsCard?.repeats ?: 0,
                        firstReview = firstReview,
                        firstReviewSrsDate = firstReview?.toSrsDate(),
                        lastReview = card?.lastReview,
                        lastReviewSrsDate = card?.lastReview?.toSrsDate(),
                        expectedReviewDate = card?.expectedReview?.toSrsDate()
                    )
                }
                PracticeTypeDeckData(itemsData = itemsData)
            }

            VocabSrsDeckDescriptor(
                id = it.id,
                title = it.title,
                position = it.position,
                lastReview = reviewHistoryRepository.getDeckLastReview(
                    deckId = it.id,
                    practiceTypes = VocabPracticeType.srsPracticeTypeValues
                ),
                items = items,
                itemsData = itemsDataMap
            )
        }
    }

    override suspend fun getDeckSortConfiguration(): DeckSortConfiguration {
        return DeckSortConfiguration(
            sortByReviewDate = userPreferencesRepository.vocabDashboardSortByTime.get()
        )
    }

    override suspend fun getDeckLimit(
        configuration: DailyLimitConfiguration,
        newDoneToday: Int,
        dueDoneToday: Int
    ): DeckLimit.EnabledDeckLimit {
        return when {
            configuration.isVocabLimitCombined -> DeckLimit.Combined(
                limit = configuration.vocabCombinedLimit,
                newDone = newDoneToday,
                dueDone = dueDoneToday
            )

            else -> DeckLimit.Separate(
                limitsMap = configuration.vocabSeparatedLimit
            )
        }
    }

    override fun createDeck(
        deckDescriptor: VocabSrsDeckDescriptor,
        deckLimit: DeckLimit,
        currentSrsDate: LocalDate
    ): VocabSrsDeck {
        return VocabSrsDeck(
            id = deckDescriptor.id,
            title = deckDescriptor.title,
            position = deckDescriptor.position,
            lastReview = deckDescriptor.lastReview,
            items = deckDescriptor.items,
            progressMap = deckDescriptor.itemsData.mapValues { (practiceType, deckData) ->
                deckData.toProgress(deckLimit, practiceType, currentSrsDate)
            }
        )
    }

}