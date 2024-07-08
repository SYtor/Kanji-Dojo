package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Path
import kotlinx.coroutines.CoroutineScope
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.stroke_evaluator.KanjiStrokeEvaluator
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.DefaultCharacterWriterState
import kotlin.time.Duration


data class VocabQueueProgress(
    val pending: Int,
    val repeats: Int,
    val completed: Int
)

sealed interface VocabQueueItemDescriptor {

    val wordId: Long
    val practiceType: VocabPracticeType

    data class Flashcard(
        override val wordId: Long,
        val priority: VocabPracticeReadingPriority,
        val translationInFont: Boolean
    ) : VocabQueueItemDescriptor {
        override val practiceType: VocabPracticeType = VocabPracticeType.Flashcard
    }

    data class ReadingPicker(
        override val wordId: Long,
        val priority: VocabPracticeReadingPriority,
        val showMeaning: Boolean
    ) : VocabQueueItemDescriptor {
        override val practiceType: VocabPracticeType = VocabPracticeType.ReadingPicker
    }

    data class Writing(
        override val wordId: Long,
        val priority: VocabPracticeReadingPriority
    ) : VocabQueueItemDescriptor {
        override val practiceType: VocabPracticeType = VocabPracticeType.Writing
    }

}

sealed interface VocabReviewQueueState {

    object Loading : VocabReviewQueueState

    data class Review(
        val state: MutableVocabReviewState,
        val progress: VocabQueueProgress,
        val answers: VocabPracticeSrsAnswers
    ) : VocabReviewQueueState

    data class Summary(
        val duration: Duration,
        val items: List<VocabSummaryItem>
    ) : VocabReviewQueueState

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
        val word: JapaneseWord,
        val reading: FuriganaString,
        val noFuriganaReading: FuriganaString,
        val meaning: String,
        val showMeaningInFront: Boolean,
    ) : VocabPracticeItemData {

        override fun toReviewState(
            coroutineScope: CoroutineScope
        ) = MutableVocabReviewState.Flashcard(
            word, reading, noFuriganaReading, meaning, showMeaningInFront
        )

    }

    data class Reading(
        val word: JapaneseWord,
        val questionCharacter: String,
        val revealedReading: FuriganaString,
        val hiddenReading: FuriganaString,
        val answers: List<String>,
        val correctAnswer: String,
        val showMeaning: Boolean,
    ) : VocabPracticeItemData {
        override fun toReviewState(
            coroutineScope: CoroutineScope
        ) = MutableVocabReviewState.Reading(
            word,
            questionCharacter,
            revealedReading,
            hiddenReading,
            answers,
            correctAnswer,
            showMeaning
        )
    }

    data class Writing(
        val word: JapaneseWord,
        val summaryReading: FuriganaString,
        val writerData: List<Pair<String, CharacterWriterData?>>
    ) : VocabPracticeItemData {

        override fun toReviewState(
            coroutineScope: CoroutineScope
        ) = MutableVocabReviewState.Writing(
            word = word,
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
            }
        )

    }

}


sealed interface MutableVocabReviewState {

    val asImmutable: VocabReviewState

    val word: JapaneseWord
    val summaryReading: FuriganaString

    class Flashcard(
        override val word: JapaneseWord,
        override val reading: FuriganaString,
        override val noFuriganaReading: FuriganaString,
        override val meaning: String,
        override val showMeaningInFront: Boolean,
    ) : MutableVocabReviewState, VocabReviewState.Flashcard {

        override val showAnswer: MutableState<Boolean> = mutableStateOf(false)

        override val summaryReading: FuriganaString = reading
        override val asImmutable: VocabReviewState.Flashcard = this

    }

    class Reading(
        override val word: JapaneseWord,
        override val questionCharacter: String,
        val revealedReading: FuriganaString,
        val hiddenReading: FuriganaString,
        override val answers: List<String>,
        override val correctAnswer: String,
        override val showMeaning: Boolean,
    ) : MutableVocabReviewState, VocabReviewState.Reading {

        override val asImmutable: VocabReviewState.Reading = this

        override val summaryReading: FuriganaString = revealedReading
        override val displayReading = mutableStateOf<FuriganaString>(hiddenReading)
        override val selectedAnswer = mutableStateOf<SelectedReadingAnswer?>(null)

        fun isCorrectAnswer(): Boolean? = selectedAnswer.value?.isCorrect

    }

    class Writing(
        override val word: JapaneseWord,
        override val summaryReading: FuriganaString,
        override val charactersData: List<VocabCharacterWritingData>,
    ) : MutableVocabReviewState, VocabReviewState.Writing {
        override val asImmutable: VocabReviewState.Writing = this
        override val selected: MutableState<VocabCharacterWritingData> = mutableStateOf(
            value = charactersData.firstOrNull { it is VocabCharacterWritingData.WithStrokes }
                ?: charactersData.first()
        )
    }

}