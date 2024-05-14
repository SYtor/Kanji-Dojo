package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

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
            val practiceType: VocabPracticeType
        ) : ScreenState

        data class Review(
            val reviewState: StateFlow<VocabReviewState>
        ) : ScreenState

        data class Summary(
            val practiceDuration: Duration
        ) : ScreenState

    }

}