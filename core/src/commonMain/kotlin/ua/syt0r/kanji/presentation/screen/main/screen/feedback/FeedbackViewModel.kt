package ua.syt0r.kanji.presentation.screen.main.screen.feedback

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.feedback.FeedbackManager
import ua.syt0r.kanji.core.feedback.FeedbackRequestData

class FeedbackViewModel(
    private val viewModelScope: CoroutineScope,
    private val feedbackManager: FeedbackManager
) : FeedbackScreenContract.ViewModel {

    private val _feedbackState = MutableStateFlow<FeedbackState>(FeedbackState.Editing)
    override val feedbackState: StateFlow<FeedbackState> = _feedbackState

    private val _errorFlow = MutableSharedFlow<String?>()
    override val errorFlow: SharedFlow<String?> = _errorFlow

    override fun sendFeedback(data: FeedbackScreenSubmitData) {
        _feedbackState.value = FeedbackState.Sending
        viewModelScope.launch {
            val requestData = FeedbackRequestData(data.topic, data.message)
            val result = feedbackManager.sendFeedback(requestData)

            if (result.isSuccess) {
                _feedbackState.value = FeedbackState.Completed
            } else {
                _errorFlow.emit(result.exceptionOrNull()?.message)
                _feedbackState.value = FeedbackState.Editing
            }
        }
    }

}
