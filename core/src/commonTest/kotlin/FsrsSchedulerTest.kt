import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ua.syt0r.kanji.core.srs.fsrs.DefaultFsrsScheduler
import ua.syt0r.kanji.core.srs.fsrs.Fsrs45
import ua.syt0r.kanji.core.srs.fsrs.FsrsAlgorithm
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating
import ua.syt0r.kanji.core.srs.fsrs.FsrsScheduler
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.DurationUnit

/***
 * Tests according to https://github.com/open-spaced-repetition/go-fsrs/blob/main/fsrs_test.go
 * Almost matching :)
 */
class FsrsSchedulerTest {

    lateinit var fsrsAlgorithm: FsrsAlgorithm
    lateinit var scheduler: FsrsScheduler
    lateinit var now: Instant

    val testWeights = arrayOf(
        1.0171, 1.8296, 4.4145, 10.9355, 5.0965, 1.3322, 1.017, 0.0, 1.6243, 0.1369, 1.0321,
        2.1866, 0.0661, 0.336, 1.7766, 0.1693, 2.9244
    )

    @BeforeTest
    fun setup() {
        fsrsAlgorithm = Fsrs45(w = testWeights)
        scheduler = DefaultFsrsScheduler(fsrsAlgorithm)
        now = LocalDateTime(2022, 11, 29, 12, 30)
            .toInstant(TimeZone.UTC)
    }

    @Test
    fun intervalsTest() {
        val answers = listOf(
            FsrsReviewRating.Good,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good,
            FsrsReviewRating.Again,
            FsrsReviewRating.Again,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good
        )

        val srsCards = getDataForAnswers(answers)

        // original listOf(0, 0, 4, 15, 49, 143, 379, 0, 0, 15, 37, 85, 184, 376)
        val expectedIntervals = listOf(0, 4, 15, 48, 140, 372, 0, 0, 14, 35, 81, 176, 361)
        val resultIntervals = srsCards.map { it.interval.toDouble(DurationUnit.DAYS).toInt() }

        val expectedStatuses = listOf(
            FsrsCardStatus.Learning,
            FsrsCardStatus.Review,
            FsrsCardStatus.Review,
            FsrsCardStatus.Review,
            FsrsCardStatus.Review,
            FsrsCardStatus.Review,
            FsrsCardStatus.Relearning,
            FsrsCardStatus.Relearning,
            FsrsCardStatus.Review,
            FsrsCardStatus.Review,
            FsrsCardStatus.Review,
            FsrsCardStatus.Review,
            FsrsCardStatus.Review
        )
        val resultStatuses = srsCards.map { it.status }

        assertEquals(expectedIntervals, resultIntervals)
        assertEquals(expectedStatuses, resultStatuses)

    }

    @Test
    fun memoryStateTest() {
        val answers = listOf(
            FsrsReviewRating.Again,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good,
            FsrsReviewRating.Good
        )

        val cardData = getDataForAnswers(answers)

        // original listOf(0, 0, 1, 3, 8, 21)
        val expectedIntervals = listOf(0, 1, 2, 5, 13, 29)
        val resultIntervals = cardData.map { it.interval.inWholeDays.toInt() }

        val lastCard = cardData.last().let { it.params as FsrsCardParams.Existing }

        val expectStability = 29.45923033704813 // original 43.0554
        val resultStability = lastCard.stability

        val expectDifficulty = 7.7608999999999995 // original 7.7609
        val resultDifficulty = lastCard.difficulty

        assertEquals(expectedIntervals, resultIntervals)
        assertEquals(expectStability, resultStability)
        assertEquals(expectDifficulty, resultDifficulty)
    }

    private fun getDataForAnswers(answers: List<FsrsReviewRating>): List<FsrsCard> {
        var srsCard = scheduler.newCard()

        return answers.map { answer ->
            srsCard = scheduler.schedule(srsCard, now).run {
                when (answer) {
                    FsrsReviewRating.Again -> again
                    FsrsReviewRating.Hard -> hard
                    FsrsReviewRating.Good -> good
                    FsrsReviewRating.Easy -> easy
                }
            }
            srsCard.also { now += it.interval }
        }
    }

}