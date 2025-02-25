package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.flow
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackScreenUI
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackState
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic

@Composable
private fun BasePreview(
    feedbackState: FeedbackState,
    feedbackTopic: FeedbackTopic = FeedbackTopic.General
) {
    AppTheme {
        FeedbackScreenUI(
            feedbackTopic = feedbackTopic,
            feedbackState = rememberUpdatedState(feedbackState),
            errorFlow = flow { },
            navigateBack = {},
            submitFeedback = {}
        )
    }
}

@Preview
@Composable
private fun IdlePreview() {
    BasePreview(feedbackState = FeedbackState.Editing)
}

@Preview
@Composable
private fun CompletePreview() {
    BasePreview(feedbackState = FeedbackState.Completed)
}