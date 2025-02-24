package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackScreen
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic
import ua.syt0r.kanji.presentation.screen.main.screen.info.toInfoScreenData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui.LetterPracticeScreenUI

object DefaultLetterPracticeScreenContent : LetterPracticeScreenContract.Content {

    @Composable
    override fun invoke(
        configuration: LetterPracticeScreenConfiguration,
        mainNavigationState: MainNavigationState,
        viewModel: LetterPracticeScreenContract.ViewModel
    ) {

        LaunchedEffect(Unit) {
            viewModel.initialize(configuration)
        }

        LetterPracticeScreenUI(
            state = viewModel.state,
            navigateBack = { mainNavigationState.navigateBack() },
            navigateToWordFeedback = {
                val feedbackTopic = FeedbackTopic.Expression(it.id, FeedbackScreen.WritingPractice)
                val destination = MainDestination.Feedback(feedbackTopic)
                mainNavigationState.navigate(destination)
            },
            onConfigured = { viewModel.configure() },
            speakKana = { viewModel.speakKana(it) },
            onNextClick = { viewModel.submitAnswer(it) },
            onWordClick = {
                val destination = MainDestination.Info(it.toInfoScreenData())
                mainNavigationState.navigate(destination)
            },
            finishPractice = { viewModel.finishPractice() },
            onPracticeCompleted = { mainNavigationState.navigateBack() },
        )

    }

}