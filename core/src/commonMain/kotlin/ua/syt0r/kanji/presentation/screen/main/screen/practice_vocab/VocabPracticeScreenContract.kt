package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.srs.SrsCard
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationItemsSelectorState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeReadingPriority
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabSummaryItem
import kotlin.time.Duration

interface VocabPracticeScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>

        fun initialize(configuration: VocabPracticeScreenConfiguration)
        fun configure()

        fun revealFlashcard()
        fun submitReadingPickerAnswer(answer: String)
        fun next(srsCard: SrsCard)
        fun finishPractice()

    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Configuration(
            val practiceType: VocabPracticeType,
            val itemsSelectorState: PracticeConfigurationItemsSelectorState<Long>,
            val shuffle: MutableState<Boolean>,
            val readingPriority: MutableState<VocabPracticeReadingPriority>,
            val flashcard: VocabPracticeConfiguration.Flashcard,
            val readingPicker: VocabPracticeConfiguration.ReadingPicker
        ) : ScreenState

        data class Review(
            val state: State<VocabPracticeReviewState>
        ) : ScreenState

        data class Summary(
            val practiceDuration: Duration,
            val results: List<VocabSummaryItem>
        ) : ScreenState

    }

}