package ua.syt0r.kanji.presentation.screen.main.screen.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
fun FeedbackScreen(
    feedbackTopic: FeedbackTopic,
    mainNavigationState: MainNavigationState,
    viewModel: FeedbackScreenContract.ViewModel = getMultiplatformViewModel()
) {

    FeedbackScreenUI(
        feedbackTopic = feedbackTopic,
        feedbackState = viewModel.feedbackState.collectAsState(),
        errorFlow = viewModel.errorFlow,
        navigateBack = { mainNavigationState.navigateBack() },
        submitFeedback = { viewModel.sendFeedback(it) }
    )

}
