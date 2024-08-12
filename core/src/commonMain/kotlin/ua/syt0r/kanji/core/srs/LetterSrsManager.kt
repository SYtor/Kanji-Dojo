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
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.Practice
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterPracticeType
import kotlin.math.max
import kotlin.math.min

interface LetterSrsManager {
    val dataChangeFlow: SharedFlow<Unit>
    suspend fun getUpdatedDecksData(): LetterSrsDecksData
    suspend fun getUpdatedDeckInfo(deckId: Long): LetterSrsDeckInfo
    suspend fun getStatus(letter: String, practiceType: PreferencesLetterPracticeType): CharacterSrsData
}

class DefaultLetterSrsManager(
    private val dailyLimitManager: DailyLimitManager,
    private val practiceRepository: LetterPracticeRepository,
    private val studyProgressCache: CharacterStudyProgressCache,
    private val getDeckSrsProgressUseCase: GetLetterDeckSrsProgressUseCase,
    private val getLetterSrsStatusUseCase: GetLetterSrsStatusUseCase,
    private val timeUtils: TimeUtils,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined),
) : LetterSrsManager {

    private val _dataChangeFlow = MutableSharedFlow<Unit>()
    override val dataChangeFlow: SharedFlow<Unit> = _dataChangeFlow

    init {
        val dataChangeFlowWithCacheClearing = mergeSharedFlows(
            coroutineScope,
            practiceRepository.changesFlow.onEach { studyProgressCache.clear() },
            dailyLimitManager.changesFlow,
        )

        dataChangeFlowWithCacheClearing
            .onEach { _dataChangeFlow.emit(it) }
            .launchIn(coroutineScope)
    }

    override suspend fun getUpdatedDecksData(): LetterSrsDecksData {
        val dailyLimitConfiguration = dailyLimitManager.getConfiguration()
        val currentDate = getSrsDate()

        val decks = practiceRepository.getAllPractices()
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
        practiceType: PreferencesLetterPracticeType,
    ): CharacterSrsData {
        return getLetterSrsStatusUseCase(letter, practiceType, getSrsDate())
    }

    private suspend fun getDailyProgress(
        date: LocalDate,
        decks: List<Practice>,
        dailyLimitConfiguration: DailyLimitConfiguration
    ): DailyProgress {

        val studyProgressList = studyProgressCache.get()
        val letterToStudyProgressList = decks
            .flatMap { practiceRepository.getKanjiForPractice(it.id) }
            .distinct()
            .map { it to studyProgressList[it] }

        val letterPracticeTypesCount = 2

        val totalNew = letterToStudyProgressList.sumOf { (_, progressList) ->
            if (progressList == null) letterPracticeTypesCount
            else letterPracticeTypesCount - progressList.size
        }

        val totalDue = studyProgressList
            .flatMap { it.value }
            .count {
                val srsData = getLetterSrsStatusUseCase(it.character, it.practiceType, date)
                srsData.status == SrsItemStatus.Review
            }

        val progressListUpdatedToday = studyProgressList.asSequence()
            .flatMap { it.value }
            .filter { getSrsDate(it.lastReviewTime) == date }
            .toList()

        val newReviewedToday = progressListUpdatedToday.filter {
            practiceRepository.getFirstReviewTime(it.character, it.practiceType)
                ?.let { getSrsDate(it) } == date
        }

        val dueReviewedToday = progressListUpdatedToday.size - newReviewedToday.size

        val newLeft: Int
        val dueLeft: Int

        when (dailyLimitConfiguration.enabled) {
            true -> {
                newLeft = max(
                    a = 0,
                    b = min(dailyLimitConfiguration.newLimit - newReviewedToday.size, totalNew)
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
            newReviewed = newReviewedToday.size,
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