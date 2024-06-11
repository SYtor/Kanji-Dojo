package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import ua.syt0r.kanji.core.RefreshableData
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.refreshableDataFlow
import ua.syt0r.kanji.core.srs.LetterSrsManager
import ua.syt0r.kanji.core.time.TimeUtils
import ua.syt0r.kanji.core.user_data.practice.CharacterReviewResult
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

interface SubscribeOnStatsDataUseCase {
    operator fun invoke(invalidationRequests: Flow<Unit>): Flow<RefreshableData<StatsData>>
}

data class StatsData(
    val today: LocalDate,
    val yearlyPractices: Map<LocalDate, Int>,
    val todayReviews: Int,
    val todayTimeSpent: Duration,
    val totalReviews: Int,
    val totalTimeSpent: Duration,
    val totalCharactersStudied: Int
)

class DefaultSubscribeOnStatsDataUseCase(
    private val letterSrsManager: LetterSrsManager,
    private val letterPracticeRepository: LetterPracticeRepository,
    private val timeUtils: TimeUtils
) : SubscribeOnStatsDataUseCase {

    override fun invoke(
        invalidationRequests: Flow<Unit>
    ): Flow<RefreshableData<StatsData>> {
        return refreshableDataFlow(
            dataChangeFlow = letterSrsManager.dataChangeFlow,
            invalidationRequestsFlow = invalidationRequests,
            provider = { getStats() }
        )
    }

    private suspend fun getStats(): StatsData {
        Logger.logMethod()
        val today = timeUtils.getCurrentDate()

        val timeZone = TimeZone.currentSystemDefault()
        val periodStart = LocalDate(today.year, 1, 1).atStartOfDayIn(timeZone)
        val periodEnd = LocalDate(today.year + 1, 1, 1).atStartOfDayIn(timeZone)

        val reviews = letterPracticeRepository.getReviews(periodStart, periodEnd)
            .mapValues { (_, instant) -> instant.toLocalDateTime(timeZone).date }
            .toList()

        val dateToReviews: Map<LocalDate, List<CharacterReviewResult>> = reviews
            .groupBy { it.second }
            .toList()
            .associate { it.first to it.second.map { it.first } }

        val todayReviews = dateToReviews[today] ?: emptyList()

        return StatsData(
            today = today,
            yearlyPractices = dateToReviews.mapValues { (_, practices) -> practices.size },
            todayReviews = todayReviews.size,
            todayTimeSpent = todayReviews.map { it.reviewDuration }
                .fold(Duration.ZERO) { acc, duration ->
                    acc.plus(
                        min(
                            a = duration.inWholeMilliseconds,
                            b = SingleDurationLimit.inWholeMilliseconds
                        ).milliseconds
                    )
                },
            totalReviews = letterPracticeRepository
                .getTotalReviewsCount()
                .toInt(),
            totalTimeSpent = letterPracticeRepository
                .getTotalPracticeTime(SingleDurationLimit.inWholeMilliseconds)
                .milliseconds,
            totalCharactersStudied = letterPracticeRepository
                .getTotalUniqueReviewedCharactersCount()
                .toInt()
        )
    }

    companion object {
        private val SingleDurationLimit = 1.minutes
    }

}