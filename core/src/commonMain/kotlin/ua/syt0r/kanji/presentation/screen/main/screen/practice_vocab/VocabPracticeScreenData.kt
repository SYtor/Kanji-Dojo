package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import kotlin.time.Duration

enum class VocabPracticeType {
    ReadingPicker
}

data class VocabPracticeConfiguration(
    val practiceType: VocabPracticeType
)

sealed interface VocabReviewState {

    object Loading : VocabReviewState

    interface Reading : VocabReviewState {
        val vocab: StateFlow<FuriganaString>
        val answers: List<String>
        val correctAnswer: String
        val selectedAnswer: StateFlow<String?>
    }

    data class Summary(
        val duration: Duration
    ) : VocabReviewState

}

data class VocabQueueItemDescriptor(
    val id: Long,
    val practiceType: VocabPracticeType
)