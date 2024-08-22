package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.mergeSharedFlows
import ua.syt0r.kanji.core.srs.use_case.GetLetterDeckSrsProgressUseCase
import ua.syt0r.kanji.core.srs.use_case.GetLetterSrsStatusUseCase
import ua.syt0r.kanji.core.srs.use_case.GetSrsStatusUseCase
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.Deck
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.ReviewHistoryRepository
import kotlin.math.max
import kotlin.math.min

interface LetterSrsManager {
    val dataChangeFlow: SharedFlow<Unit>
    suspend fun getUpdatedDecksData(): LetterSrsDecksData
    suspend fun getUpdatedDeckInfo(deckId: Long): LetterSrsDeckInfo
    suspend fun getStatus(letter: String, practiceType: LetterPracticeType): CharacterSrsData
}

class DefaultLetterSrsManager(
    private val dailyLimitManager: DailyLimitManager,
    private val practiceRepository: LetterPracticeRepository,
    private val srsItemRepository: SrsItemRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val getDeckSrsProgressUseCase: GetLetterDeckSrsProgressUseCase,
    private val getLetterSrsStatusUseCase: GetLetterSrsStatusUseCase,
    private val getSrsStatusUseCase: GetSrsStatusUseCase,
    private val timeUtils: TimeUtils,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) : LetterSrsManager {

    private val _dataChangeFlow = MutableSharedFlow<Unit>()
    override val dataChangeFlow: SharedFlow<Unit> = _dataChangeFlow

    private val supportedLetterPracticeTypeValues = LetterPracticeType.srsPracticeTypeValues

    init {
        val dataChangeFlowWithCacheClearing = mergeSharedFlows(
            coroutineScope,
            practiceRepository.changesFlow,
            dailyLimitManager.changesFlow,
            srsItemRepository.updatesFlow
        )

        dataChangeFlowWithCacheClearing
            .onEach { _dataChangeFlow.emit(it) }
            .launchIn(coroutineScope)
    }

    override suspend fun getUpdatedDecksData(): LetterSrsDecksData {
        val dailyLimitConfiguration = dailyLimitManager.getConfiguration()
        val currentDate = getSrsDate()

        val decks = practiceRepository.getDecks()
        val dailyProgress = getDailyProgress(currentDate, decks, dailyLimitConfiguration)

        val decksInfo = decks.map { getDeckSrsProgressUseCase(it.id, currentDate) }

        return LetterSrsDecksData(
            decks = decksInfo,
            dailyLimitConfiguration = dailyLimitConfiguration,
            dailyProgress = dailyProgress
        )
    }

    override suspend fun getUpdatedDeckInfo(deckId: Long): LetterSrsDeckInfo {
        return getDeckSrsProgressUseCase(
            deckId = deckId,
            srsDate = getSrsDate()
        )
    }

    override suspend fun getStatus(
        letter: String,
        practiceType: LetterPracticeType,
    ): CharacterSrsData {
        return getLetterSrsStatusUseCase(letter, practiceType, getSrsDate())
    }

    private suspend fun getDailyProgress(
        date: LocalDate,
        decks: List<Deck>,
        dailyLimitConfiguration: DailyLimitConfiguration
    ): DailyProgress {

        val allSrsItems = decks
            .flatMap { practiceRepository.getDeckCharacters(it.id) }
            .distinct()

        val allSrsItemKeys = allSrsItems
            .flatMap { letter -> supportedLetterPracticeTypeValues.map { SrsCardKey(letter, it) } }
            .toSet()

        val reviewedSrsItemsMap = srsItemRepository.getAll()
            .filter { it.key.practiceType in supportedLetterPracticeTypeValues }

        val totalNew = allSrsItemKeys.minus(reviewedSrsItemsMap.keys).size

        val totalDue = reviewedSrsItemsMap
            .filter {
                getSrsStatusUseCase(it.value.lastReview!! + it.value.interval) == SrsItemStatus.Review
            }
            .size

        val srsItemsReviewedToday = reviewedSrsItemsMap
            .filter { getSrsDate(it.value.lastReview!!) == date }

        val newSrsItemsReviewedToday = srsItemsReviewedToday.filter { (srsCardKey, _) ->
            val firstReviewTime = reviewHistoryRepository.getFirstReviewTime(
                key = srsCardKey.itemKey,
                practiceType = srsCardKey.practiceType
            )!!
            getSrsDate(firstReviewTime) == date
        }

        val newReviewedToday = newSrsItemsReviewedToday.size
        val dueReviewedToday = srsItemsReviewedToday.keys.minus(newSrsItemsReviewedToday.keys).size

        val newLeft: Int
        val dueLeft: Int

        when (dailyLimitConfiguration.enabled) {
            true -> {
                newLeft = max(
                    a = 0,
                    b = min(dailyLimitConfiguration.newLimit - newReviewedToday, totalNew)
                )

                dueLeft = max(
                    a = 0,
                    b = min(dailyLimitConfiguration.dueLimit - dueReviewedToday, totalDue)
                )
            }

            false -> {
                newLeft = totalNew
                dueLeft = totalDue
            }
        }

        return DailyProgress(
            newReviewed = newReviewedToday,
            dueReviewed = dueReviewedToday,
            newLeft = newLeft,
            dueLeft = dueLeft
        )
    }

    private fun getSrsDate(
        instant: Instant = timeUtils.now(),
    ): LocalDate {
        return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

}