package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryRepository
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository

interface LetterSrsManager {
    val dataChangeFlow: SharedFlow<Unit>
    suspend fun getDecks(): LetterSrsDecksData
    suspend fun getDeck(deckId: Long): LetterSrsDeck
}

typealias LetterSrsDecksData = SrsDecksData<LetterSrsDeck, LetterPracticeType>
typealias LetterSrsDeckDescriptor = SrsDeckDescriptor<String, LetterPracticeType>
typealias LetterSrsDeckProgress = SrsDeckProgress<String>

data class LetterSrsDeck(
    override val id: Long,
    override val title: String,
    override val position: Int,
    override val items: List<String>,
    override val lastReview: Instant?,
    override val progressMap: Map<LetterPracticeType, LetterSrsDeckProgress>
) : SrsDeckData<LetterPracticeType, String>

class DefaultLetterSrsManager(
    private val practiceRepository: LetterPracticeRepository,
    private val srsItemRepository: SrsItemRepository,
    dailyLimitManager: DailyLimitManager,
    timeUtils: TimeUtils,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    coroutineScope: CoroutineScope
) : SrsManager<String, LetterPracticeType, LetterSrsDeck>(
    deckChangesFlow = practiceRepository.changesFlow,
    dailyLimitManager = dailyLimitManager,
    timeUtils = timeUtils,
    coroutineScope = coroutineScope
), LetterSrsManager {

    override val practiceTypes: List<LetterPracticeType> = LetterPracticeType.values().toList()

    override suspend fun getDecks(): LetterSrsDecksData {
        return getDecksInternal()
    }

    override suspend fun getDeck(deckId: Long): LetterSrsDeck {
        return getDecksInternal().decks.first { it.id == deckId }
    }

    override suspend fun getDeckDescriptors(): List<LetterSrsDeckDescriptor> {
        return practiceRepository.getDecks().map {
            val items = practiceRepository.getDeckCharacters(it.id)

            val itemsDataMap = LetterPracticeType.values().associateWith { practiceType ->
                val itemsData: Map<String, SrsCardData> = items.associateWith { letter ->
                    val key = practiceType.toSrsKey(letter)
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

            LetterSrsDeckDescriptor(
                id = it.id,
                title = it.name,
                position = it.position,
                lastReview = reviewHistoryRepository.getDeckLastReview(
                    deckId = it.id,
                    practiceTypes = LetterPracticeType.srsPracticeTypeValues
                ),
                items = items,
                itemsData = itemsDataMap
            )
        }
    }

    override suspend fun getDeckSortConfiguration(): DeckSortConfiguration {
        return DeckSortConfiguration(
            sortByReviewDate = userPreferencesRepository.dashboardSortByTime.get()
        )
    }

    override suspend fun getDeckLimit(
        configuration: DailyLimitConfiguration,
        newDoneToday: Int,
        dueDoneToday: Int
    ): DeckLimit.EnabledDeckLimit {
        return when {
            configuration.isLetterLimitCombined -> DeckLimit.Combined(
                limit = configuration.letterCombinedLimit,
                newDone = newDoneToday,
                dueDone = dueDoneToday
            )

            else -> DeckLimit.Separate(
                limitsMap = configuration.letterSeparatedLimit
            )
        }
    }

    override fun createDeck(
        deckDescriptor: LetterSrsDeckDescriptor,
        deckLimit: DeckLimit,
        currentSrsDate: LocalDate
    ): LetterSrsDeck {
        return LetterSrsDeck(
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
