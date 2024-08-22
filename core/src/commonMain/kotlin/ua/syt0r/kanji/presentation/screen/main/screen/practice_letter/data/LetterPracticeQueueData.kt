package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Deferred
import ua.syt0r.kanji.core.srs.LetterPracticeType
import ua.syt0r.kanji.core.srs.SrsCard
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.stroke_evaluator.KanjiStrokeEvaluator
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueueItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueueProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryItem
import kotlin.time.Duration


sealed interface LetterPracticeQueueState {

    object Loading : LetterPracticeQueueState

    data class Review(
        val descriptor: LetterPracticeQueueItemDescriptor,
        val data: LetterPracticeItemData,
        val currentItemRepeat: Int,
        val progress: PracticeQueueProgress,
        val answers: PracticeAnswers,
        val totalMistakes: MutableState<Int>,
        val currentReviewMistakes: MutableState<Int>
    ) : LetterPracticeQueueState

    data class Summary(
        val duration: Duration,
        val items: List<LetterPracticeSummaryItem>
    ) : LetterPracticeQueueState

}

sealed interface LetterPracticeQueueItemDescriptor {

    val character: String
    val practiceType: LetterPracticeType
    val deckId: Long
    val romajiReading: Boolean
    val layoutConfiguration: LetterPracticeLayoutConfiguration

    data class Writing(
        override val character: String,
        override val deckId: Long,
        override val romajiReading: Boolean,
        override val layoutConfiguration: LetterPracticeLayoutConfiguration.WritingLayoutConfiguration,
        val inputMode: WritingPracticeInputMode,
        val evaluator: KanjiStrokeEvaluator,
        val shouldStudy: Boolean
    ) : LetterPracticeQueueItemDescriptor {
        override val practiceType = LetterPracticeType.Writing
    }

}

data class LetterPracticeQueueItem(
    val descriptor: LetterPracticeQueueItemDescriptor,
    override val srsCardKey: SrsCardKey,
    override val srsCard: SrsCard,
    override val deckId: Long,
    override val repeats: Int,
    override val data: Deferred<LetterPracticeItemData>,
    val totalMistakes: MutableState<Int> // TODO better extra review data handling
) : PracticeQueueItem<LetterPracticeQueueItem> {

    val currentReviewMistakes: MutableState<Int> = mutableStateOf(0)

    override fun copyForRepeat(srsCard: SrsCard): LetterPracticeQueueItem {
        return copy(srsCard = srsCard, repeats = repeats + 1)
    }

}

sealed interface LetterPracticeSummaryItem : PracticeSummaryItem {

    val letter: String
    override val nextInterval: Duration

    data class Writing(
        override val letter: String,
        override val nextInterval: Duration,
        val strokeCount: Int,
        val mistakes: Int
    ) : LetterPracticeSummaryItem

}
