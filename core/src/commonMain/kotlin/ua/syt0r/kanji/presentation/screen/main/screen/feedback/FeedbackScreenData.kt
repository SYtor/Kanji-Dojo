package ua.syt0r.kanji.presentation.screen.main.screen.feedback

import kotlinx.serialization.Serializable

enum class FeedbackScreen {
    WritingPractice, ReadingPractice, Search, CharacterInfo, VocabPractice
}

@Serializable
sealed interface FeedbackTopic {

    @Serializable
    object General : FeedbackTopic

    @Serializable
    data class Expression(
        val id: Long,
        val screen: FeedbackScreen
    ) : FeedbackTopic

}

enum class FeedbackState { Editing, Sending, Completed }

data class FeedbackScreenSubmitData(
    val topic: String,
    val message: String
)