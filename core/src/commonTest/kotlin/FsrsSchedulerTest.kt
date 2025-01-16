import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ua.syt0r.kanji.core.srs.fsrs.DefaultFsrsScheduler
import ua.syt0r.kanji.core.srs.fsrs.Fsrs5
import ua.syt0r.kanji.core.srs.fsrs.FsrsAlgorithm
import ua.syt0r.kanji.core.srs.fsrs.FsrsAnswers
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardParams
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus.Learning
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus.Relearning
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus.Review
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating.Again
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating.Easy
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating.Good
import ua.syt0r.kanji.core.srs.fsrs.FsrsReviewRating.Hard
import ua.syt0r.kanji.core.srs.fsrs.FsrsScheduler
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

/***
 * Tests according to https://github.com/open-spaced-repetition/go-fsrs/blob/main/fsrs_test.go
 * Almost matching :)
 */
class FsrsSchedulerTest {

    private lateinit var fsrsAlgorithm: FsrsAlgorithm
    private lateinit var scheduler: FsrsScheduler
    private lateinit var now: Instant

    @BeforeTest
    fun setup() {
        fsrsAlgorithm = Fsrs5()
        scheduler = DefaultFsrsScheduler(fsrsAlgorithm)
        now = LocalDateTime(2022, 11, 29, 12, 30)
            .toInstant(TimeZone.UTC)
    }

    @Test
    fun intervalsTest() {
        val ratings = listOf(
            Good, Good, Good, Good, Good, Good, Again, Again, Good, Good, Good, Good, Good
        )

        val srsCards = mutableListOf<FsrsCard>()

        var card = scheduler.newCard()
        var answers = scheduler.schedule(card, now)

        ratings.forEach { rating ->
            card = answers.get(rating)
            val params = card.params as FsrsCardParams.Existing
            val due = now + card.interval
            println(
                "%s %f %f %s %s".format(
                    rating.name,
                    params.difficulty,
                    params.stability,
                    due,
                    card.status.name
                )
            )
            srsCards.add(card)
            now = due
            answers = scheduler.schedule(card, now)
        }

        val expectedIntervals = listOf(0, 4, 14, 44, 125, 328, 0, 0, 7, 16, 34, 71, 142)
        val resultIntervals = srsCards.map { it.interval.toDouble(DurationUnit.DAYS).toInt() }

        val expectedStatuses = listOf(
            Learning, Review, Review, Review, Review, Review, Relearning, Relearning,
            Review, Review, Review, Review, Review
        )
        val resultStatuses = srsCards.map { it.status }

        assertEquals(expectedIntervals, resultIntervals)
        assertEquals(expectedStatuses, resultStatuses)
    }

    @Test
    fun memoryStateTest() {
        val ratings = listOf(Again, Good, Good, Good, Good, Good, Good)
        val reviewIntervals = listOf(0, 0, 1, 3, 8, 21, 0)

        var time = Clock.System.now()
        val lastCard = ratings.zip(reviewIntervals)
            .fold(scheduler.newCard()) { card, (rating, interval) ->
                val answers = scheduler.schedule(card, time)
                val newCard = answers.get(rating)
                time = time.plus(interval.days)
                newCard
            }

        val params = lastCard.params as FsrsCardParams.Existing

        val decimalFormat = DecimalFormat("#.####")
        decimalFormat.roundingMode = RoundingMode.FLOOR

        val expectStability = 48.4848
        val resultStability = decimalFormat.format(params.stability).toDouble()

        val expectDifficulty = 7.0865
        val resultDifficulty = decimalFormat.format(params.difficulty).toDouble()

        assertEquals(expectStability, resultStability)
        assertEquals(expectDifficulty, resultDifficulty)
    }

    private fun FsrsAnswers.get(rating: FsrsReviewRating) = when (rating) {
        Again -> again
        Hard -> hard
        Good -> good
        Easy -> easy
    }

}