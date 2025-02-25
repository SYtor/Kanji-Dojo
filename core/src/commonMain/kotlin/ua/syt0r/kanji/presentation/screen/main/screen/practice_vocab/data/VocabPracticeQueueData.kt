package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.srs.SrsCard
import ua.syt0r.kanji.core.srs.SrsCardKey
import ua.syt0r.kanji.core.stroke_evaluator.KanjiStrokeEvaluator
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DefaultCharacterWriterState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueueItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeQueueProgress
import kotlin.time.Duration

sealed interface VocabPracticeQueueState {

    data object Loading : VocabPracticeQueueState

    data class Review(
        val state: MutableVocabReviewState,
        val progress: PracticeQueueProgress,
        val answers: PracticeAnswers
    ) : VocabPracticeQueueState

    data class Summary(
        val duration: Duration,
        val items: List<VocabSummaryItem>
    ) : VocabPracticeQueueState

}

data class VocabPracticeQueueItem(
    val descriptor: VocabPracticeQueueItemDescriptor,
    override val srsCardKey: SrsCardKey,
    override val srsCard: SrsCard,
    override val deckId: Long,
    override val repeats: Int,
    override val totalMistakes: Int,
    override val data: Deferred<VocabPracticeItemData>,
) : PracticeQueueItem<VocabPracticeQueueItem> {

    override fun copyForRepeat(answer: PracticeAnswer): VocabPracticeQueueItem {
        return copy(
            srsCard = answer.srsAnswer.card,
            repeats = repeats + 1,
            totalMistakes = totalMistakes + answer.mistakes
        )
    }

}

sealed interface VocabPracticeQueueItemDescriptor {

    val cardId: Long
    val practiceType: ScreenVocabPracticeType
    val deckId: Long

    data class Flashcard(
        override val cardId: Long,
        override val deckId: Long,
        val priority: VocabPracticeReadingPriority,
        val translationInFont: Boolean
    ) : VocabPracticeQueueItemDescriptor {
        override val practiceType: ScreenVocabPracticeType = ScreenVocabPracticeType.Flashcard
    }

    data class ReadingPicker(
        override val cardId: Long,
        override val deckId: Long,
        val priority: VocabPracticeReadingPriority,
        val showMeaning: Boolean
    ) : VocabPracticeQueueItemDescriptor {
        override val practiceType: ScreenVocabPracticeType = ScreenVocabPracticeType.ReadingPicker
    }

    data class Writing(
        override val cardId: Long,
        override val deckId: Long,
        val priority: VocabPracticeReadingPriority
    ) : VocabPracticeQueueItemDescriptor {
        override val practiceType: ScreenVocabPracticeType = ScreenVocabPracticeType.Writing
    }

}

data class CharacterWriterData(
    val strokeEvaluator: KanjiStrokeEvaluator,
    val character: String,
    val strokes: List<Path>,
    val configuration: CharacterWriterConfiguration
)

sealed interface VocabPracticeItemData {

    fun toReviewState(coroutineScope: CoroutineScope): MutableVocabReviewState

    data class Flashcard(
        val reading: FuriganaString,
        val hiddenReading: FuriganaString,
        val meaning: String,
        val showMeaningInFront: Boolean,
        val vocabReference: InfoScreenData.Vocab
    ) : VocabPracticeItemData {

        override fun toReviewState(
            coroutineScope: CoroutineScope
        ) = MutableVocabReviewState.Flashcard(
            reading,
            hiddenReading,
            meaning,
            showMeaningInFront,
            vocabReference
        )

    }

    data class Reading(
        val question: String,
        val revealedReading: FuriganaString,
        val hiddenReading: FuriganaString,
        val meaning: String,
        val answers: List<String>,
        val correctAnswer: String,
        val showMeaning: Boolean,
        val vocabReference: InfoScreenData.Vocab,
    ) : VocabPracticeItemData {
        override fun toReviewState(
            coroutineScope: CoroutineScope
        ) = MutableVocabReviewState.Reading(
            question,
            revealedReading,
            hiddenReading,
            meaning,
            answers,
            correctAnswer,
            showMeaning,
            vocabReference
        )
    }

    data class Writing(
        val meaning: String,
        val summaryReading: FuriganaString,
        val writerData: List<Pair<String, CharacterWriterData?>>,
        val vocabReference: InfoScreenData.Vocab
    ) : VocabPracticeItemData {

        override fun toReviewState(
            coroutineScope: CoroutineScope
        ) = MutableVocabReviewState.Writing(
            meaning = meaning,
            summaryReading = summaryReading,
            charactersData = writerData.map { (character, writerData) ->
                if (writerData == null)
                    return@map VocabCharacterWritingData.NoStrokes(character)

                VocabCharacterWritingData.WithStrokes(
                    character = character,
                    writerState = writerData.run {
                        DefaultCharacterWriterState(
                            coroutineScope = coroutineScope,
                            strokeEvaluator = strokeEvaluator,
                            character = character,
                            strokes = strokes,
                            configuration = configuration
                        )
                    }
                )
            },
            vocabReference = vocabReference
        )

    }

}


sealed interface MutableVocabReviewState {

    val asImmutable: VocabReviewState
    val summaryReading: FuriganaString

    class Flashcard(
        override val reading: FuriganaString,
        override val noFuriganaReading: FuriganaString,
        override val meaning: String,
        override val showMeaningInFront: Boolean,
        override val vocabReference: InfoScreenData.Vocab,
    ) : MutableVocabReviewState, VocabReviewState.Flashcard {

        override val showAnswer: MutableState<Boolean> = mutableStateOf(false)

        override val summaryReading: FuriganaString = reading
        override val asImmutable: VocabReviewState.Flashcard = this

    }

    class Reading(
        override val questionCharacter: String,
        val revealedReading: FuriganaString,
        hiddenReading: FuriganaString,
        override val meaning: String,
        override val answers: List<String>,
        override val correctAnswer: String,
        override val showMeaning: Boolean,
        override val vocabReference: InfoScreenData.Vocab,
    ) : MutableVocabReviewState, VocabReviewState.Reading {

        override val asImmutable: VocabReviewState.Reading = this

        override val summaryReading: FuriganaString = revealedReading
        override val displayReading = mutableStateOf<FuriganaString>(hiddenReading)
        override val selectedAnswer = mutableStateOf<SelectedReadingAnswer?>(null)

    }

    class Writing(
        override val summaryReading: FuriganaString,
        override val charactersData: List<VocabCharacterWritingData>,
        override val meaning: String,
        override val vocabReference: InfoScreenData.Vocab,
    ) : MutableVocabReviewState, VocabReviewState.Writing {
        override val asImmutable: VocabReviewState.Writing = this
        override val selected: MutableState<VocabCharacterWritingData> = mutableStateOf(
            value = charactersData.firstOrNull { it is VocabCharacterWritingData.WithStrokes }
                ?: charactersData.first()
        )
    }

}