package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import kotlin.time.Duration


data class VocabQueueProgress(
    val current: Int,
    val total: Int
)

sealed interface VocabQueueItemDescriptor {

    data class Flashcard(
        val wordId: Long,
        val priority: VocabPracticeReadingPriority,
        val translationInFont: Boolean
    ) : VocabQueueItemDescriptor

    data class ReadingPicker(
        val wordId: Long,
        val priority: VocabPracticeReadingPriority,
        val showMeaning: Boolean
    ) : VocabQueueItemDescriptor

    data class Writing(
        val wordId: Long,
        val priority: VocabPracticeReadingPriority
    ) : VocabQueueItemDescriptor

}

sealed interface VocabReviewQueueState {

    object Loading : VocabReviewQueueState

    data class Review(
        val state: MutableVocabReviewState,
        val progress: VocabQueueProgress
    ) : VocabReviewQueueState

    data class Summary(
        val duration: Duration,
        val items: List<VocabSummaryItem>
    ) : VocabReviewQueueState

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
        override val showAnswer: MutableState<Boolean>
    ) : MutableVocabReviewState, VocabReviewState.Flashcard {
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