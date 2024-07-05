import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ua.syt0r.kanji.core.srs.fsrs.DefaultFSRS
import ua.syt0r.kanji.core.srs.fsrs.FSRS
import ua.syt0r.kanji.core.srs.fsrs.FsrsAnswer
import ua.syt0r.kanji.core.srs.fsrs.FsrsCard
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

class FsrsTest {

    lateinit var fsrs: FSRS
    lateinit var now: Instant

    @BeforeTest
    fun setup() {
        fsrs = DefaultFSRS()
        now = Clock.System.now()
    }

    @Test
    fun `when new card is reviewed as again then interval should be less than a day`() {
        val card = FsrsCard.New
        val updatedCard = fsrs.getUpdatedCard(card, FsrsAnswer.Again, now)
        val interval = fsrs.getInterval(updatedCard, now + 2.days)
        println("interval[$interval]")
        assertTrue { interval <= 1.days }
    }

    @Test
    fun `test`() {
        FsrsAnswer.values().forEach { testForAnswer(it) }
    }

    fun testForAnswer(answer: FsrsAnswer) {
        println("Testing for answer=$answer")
        var card: FsrsCard = FsrsCard.New
        (0..4).forEach { dayNumber ->
            card = fsrs.getUpdatedCard(
                card = card,
                answer = answer,
                reviewTime = now + dayNumber.days
            )
            val interval = fsrs.getInterval(
                card = card as FsrsCard.Existing,
                now = now + (dayNumber + 2).days
            )
            println("review[$dayNumber] interval[$interval]")
        }
    }

}