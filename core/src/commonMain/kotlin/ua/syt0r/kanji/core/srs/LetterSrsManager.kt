package ua.syt0r.kanji.core.srs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.mergeSharedFlows
import ua.syt0r.kanji.core.srs.use_case.GetLetterDeckSrsProgressUseCase
import ua.syt0r.kanji.core.srs.use_case.GetLetterSrsStatusUseCase
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PracticeType
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import kotlin.math.max
import kotlin.math.min

interface LetterSrsManager {
    val dataChangeFlow: SharedFlow<Unit>
    suspend fun notifyPreferencesChange()
    suspend fun getUpdatedDecksData(): LetterSrsDecksData
    suspend fun getUpdatedDeckInfo(deckId: Long): LetterSrsDeckInfo
    suspend fun getStatus(letter: String, practiceType: PracticeType): CharacterSrsData
}

class DefaultLetterSrsManager(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val practiceRepository: LetterPracticeRepository,
    private val studyProgressCache: CharacterStudyProgressCache,
    private val getDeckSrsProgressUseCase: GetLetterDeckSrsProgressUseCase,
    private val getLetterSrsStatusUseCase: GetLetterSrsStatusUseCase,
    private val timeUtils: TimeUtils,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) : LetterSrsManager {

    private val preferencesChange = Channel<Unit>()

    private val _dataChangeFlow = MutableSharedFlow<Unit>()
    override val dataChangeFlow: SharedFlow<Unit> = _dataChangeFlow

    init {
        val dataChangeFlowWithCacheClearing = mergeSharedFlows(
            coroutineScope,
            practiceRepository.changesFlow.onEach { studyProgressCache.clear() },
            preferencesChange.consumeAsFlow()
        )

        dataChangeFlowWithCacheClearing
            .onEach { _dataChangeFlow.emit(it) }
            .launchIn(coroutineScope)
    }

    override suspend fun notifyPreferencesChange() {
        preferencesChange.send(Unit)
    }

    override suspend fun getUpdatedDecksData(): LetterSrsDecksData {
        val dailyLimitConfiguration = DailyLimitConfiguration(
            enabled = userPreferencesRepository.dailyLimitEnabled.get(),
            newLimit = userPreferencesRepository.dailyLearnLimit.get(),
            dueLimit = userPreferencesRepository.dailyReviewLimit.get()
        )

        val currentDate = getSrsDate()
        val decksInfo = practiceRepository.getAllPractices()
            .map { getDeckSrsProgressUseCase(it.id, currentDate) }

        return LetterSrsDecksData(
            decks = decksInfo,
            dailyLimitConfiguration = dailyLimitConfiguration,
            dailyProgress = getDailyProgress(
                date = currentDate,
                dailyLimitConfiguration = dailyLimitConfiguration,
                decksInfo = decksInfo
            )
        )
    }

    override suspend fun getUpdatedDeckInfo(deckId: Long): LetterSrsDeckInfo {
        return getDeckSrsProgressUseCase(
            deckId = deckId,
            date = getSrsDate()
        )
    }

    override suspend fun getStatus(
        letter: String,
        practiceType: PracticeType,
    ): CharacterSrsData {
        return getLetterSrsStatusUseCase(letter, practiceType, getSrsDate())
    }

    private suspend fun getDailyProgress(
        date: LocalDate,
        dailyLimitConfiguration: DailyLimitConfiguration,
        decksInfo: List<LetterSrsDeckInfo>,
    ): LetterDailyProgress {

        val characterProgresses = studyProgressCache.get().asSequence().flatMap { it.value }

        val charactersUpdatedToday = characterProgresses
            .filter { getSrsDate(it.lastReviewTime) == date }
            .toList()

        val newReviewedToday = charactersUpdatedToday.filter {
            practiceRepository.getFirstReviewTime(it.character, it.practiceType)
                ?.let { getSrsDate(it) } == date
        }

        val reviewedToday = charactersUpdatedToday.size - newReviewedToday.size

        val totalNew = decksInfo.totalNew()
        val totalReview = decksInfo.totalReview()

        val leftToStudy = max(
            a = 0,
            b = min(dailyLimitConfiguration.newLimit - newReviewedToday.size, totalNew)
        )

        val leftToReview = max(
            a = 0,
            b = min(dailyLimitConfiguration.dueLimit - reviewedToday, totalReview)
        )

        return LetterDailyProgress(
            newReviewed = newReviewedToday.size,
            dueReviewed = reviewedToday,
            newLeft = leftToStudy,
            dueLeft = leftToReview
        )
    }

    private fun getSrsDate(
        instant: Instant = timeUtils.now(),
    ): LocalDate {
        return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    private fun List<LetterSrsDeckInfo>.totalNew(): Int {
        return flatMap { it.writingDetails.new }.distinct().size +
                flatMap { it.readingDetails.new }.distinct().size
    }


    private fun List<LetterSrsDeckInfo>.totalReview(): Int {
        return flatMap { it.writingDetails.due }.distinct().size +
                flatMap { it.readingDetails.due }.distinct().size
    }

}