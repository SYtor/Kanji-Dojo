package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface VocabPracticeScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>

        fun initialize(expressions: List<Long>)
        fun configure(configuration: VocabPracticeConfiguration)

        fun submitAnswer(answer: String)
        fun next()

        fun reportScreenShown()
    }

    sealed interface ScreenState {

        object Loading : ScreenState

        data class Configuration(
            val practiceType: VocabPracticeType,
            val shuffle: Boolean,
            val readingPriority: VocabPracticeReadingPriority,
            val showMeaning: Boolean
        ) : ScreenState

        data class Review(
            val showMeaning: Boolean,
            val practiceState: State<VocabPracticeReviewState>
        ) : ScreenState

        data class Summary(
            val practiceDuration: Duration,
            val results: List<VocabSummaryItem>
        ) : ScreenState

    }

}