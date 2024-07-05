import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.SrsItemKey
import ua.syt0r.kanji.core.srs.SrsScheduler
import ua.syt0r.kanji.core.srs.fsrs.Fsrs45Algorithm
import ua.syt0r.kanji.core.srs.fsrs.FsrsAlgorithm
import ua.syt0r.kanji.core.srs.fsrs.FsrsAnswer
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import ua.syt0r.kanji.core.srs.fsrs.FsrsCardStatus
import ua.syt0r.kanji.core.srs.fsrs.FsrsItemData
import ua.syt0r.kanji.core.srs.fsrs.FsrsScheduler
import kotlin.math.pow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.ZERO

/***
 * Tests according to https://github.com/open-spaced-repetition/go-fsrs/blob/main/fsrs_test.go
 * Almost matching :)
 */
class FsrsTest {

    lateinit var fsrsAlgorithm: FsrsAlgorithm
    lateinit var scheduler: SrsScheduler
    lateinit var now: Instant

    val testWeights = arrayOf(
        1.0171, 1.8296, 4.4145, 10.9355, 5.0965, 1.3322, 1.017, 0.0, 1.6243, 0.1369, 1.0321,
        2.1866, 0.0661, 0.336, 1.7766, 0.1693, 2.9244
    )

    @BeforeTest
    fun setup() {
        fsrsAlgorithm = Fsrs45Algorithm(
            w = testWeights,
            decay = -0.5,
            factor = 0.9.pow(1 / -0.5) - 1
        )
        scheduler = FsrsScheduler(fsrsAlgorithm)
        now = Clock.System.now()
    }

    @Test
    fun intervalsTest() {
        val answers = listOf(
            FsrsAnswer.Good,
            FsrsAnswer.Good,
            FsrsAnswer.Good,
            FsrsAnswer.Good,
            FsrsAnswer.Good,
            FsrsAnswer.Good,
            FsrsAnswer.Again,
            FsrsAnswer.Again,
            FsrsAnswer.Good,
            FsrsAnswer.Good,
            FsrsAnswer.Good,
            FsrsAnswer.Good,
            FsrsAnswer.Good
        )

        val srsItemDataList = getDataForAnswers(answers)

        // original listOf(0, 0, 4, 15, 49, 143, 379, 0, 0, 15, 37, 85, 184, 376)
        val expectedIntervals = listOf(0, 0, 4, 16, 52, 150, 398, 0, 0, 15, 37, 86, 186, 381)
        val resultIntervals = srsItemDataList.map { it.interval.inWholeDays.toInt() }

        val expectedStatuses = listOf(
            FsrsCardStatus.New,
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
        val resultStatuses = srsItemDataList.map { it.status }

        assertEquals(expectedIntervals, resultIntervals)
        assertEquals(expectedStatuses, resultStatuses)

    }

    @Test
    fun memoryStateTest() {
        val answers = listOf(
            FsrsAnswer.Again,
            FsrsAnswer.Good,
            FsrsAnswer.Good,
            FsrsAnswer.Good,
            FsrsAnswer.Good,
            FsrsAnswer.Good
        )

        val cardData = getDataForAnswers(answers)

        // original listOf(0, 0, 1, 3, 8, 21)
        val expectedIntervals = listOf(0, 0, 1, 2, 7, 17, 37)
        val resultIntervals = cardData.map { it.interval.inWholeDays.toInt() }

        val lastCard = cardData.last().let { it.card as FsrsCard.Existing }

        val expectStability = 37.748484784187355 // original 43.0554
        val resultStability = lastCard.stability

        val expectDifficulty = 7.7608999999999995 // original 7.7609
        val resultDifficulty = lastCard.difficulty

        assertEquals(expectedIntervals, resultIntervals)
        assertEquals(expectStability, resultStability)
        assertEquals(expectDifficulty, resultDifficulty)
    }

    private fun getDataForAnswers(answers: List<FsrsAnswer>): List<FsrsItemData> {
        var srsItemData = FsrsItemData(
            key = SrsItemKey("", ""),
            card = FsrsCard.New,
            status = FsrsCardStatus.New,
            lapses = 0,
            repeats = 0,
            interval = ZERO
        )

        return listOf(srsItemData) + answers.map { answer ->
            srsItemData = scheduler.get(srsItemData, answer, now) as FsrsItemData
            srsItemData.also { now += it.interval }
        }
    }

}