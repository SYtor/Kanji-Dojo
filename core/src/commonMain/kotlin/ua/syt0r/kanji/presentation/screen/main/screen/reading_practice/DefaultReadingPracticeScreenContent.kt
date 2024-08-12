package ua.syt0r.kanji.presentation.screen.main.screen.reading_practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackScreen
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ui.ReadingPracticeScreenUI

object DefaultReadingPracticeScreenContent : ReadingPracticeContract.Content {

    @Composable
    override fun Draw(
        configuration: MainDestination.Practice.Reading,
        mainNavigationState: MainNavigationState,
        viewModel: ReadingPracticeContract.ViewModel
    ) {

        LaunchedEffect(Unit) {
            viewModel.initialize(configuration)
        }

        ReadingPracticeScreenUI(
            state = viewModel.state,
            navigateBack = { mainNavigationState.navigateBack() },
            onConfigured = { viewModel.onConfigured(it) },
            onOptionSelected = { viewModel.select(it) },
            toggleKanaAutoPlay = { viewModel.toggleKanaAutoPlay() },
            speakKana = { viewModel.speakKana(it) },
            onPracticeSaveClick = { viewModel.savePractice(it) },
            onFinishButtonClick = { mainNavigationState.navigateBack() },
            onWordFeedback = {
                val feedbackTopic = FeedbackTopic.Expression(it.id, FeedbackScreen.ReadingPractice)
                val destination = MainDestination.Feedback(feedbackTopic)
                mainNavigationState.navigate(destination)
            }
        )

    }

}