package ua.syt0r.kanji.presentation.screen.main.screen.feedback

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface FeedbackScreenContract {

    interface ViewModel {
        val feedbackState: StateFlow<FeedbackState>
        val errorFlow: SharedFlow<String?>
        fun sendFeedback(data: FeedbackScreenSubmitData)
    }

}