package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackScreen
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration

@Composable
fun VocabPracticeScreen(
    configuration: VocabPracticeScreenConfiguration,
    mainNavigationState: MainNavigationState,
    viewModel: VocabPracticeScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.initialize(configuration)
    }

    VocabPracticeScreenUI(
        state = viewModel.state.collectAsState(),
        onConfigured = { viewModel.configure() },
        onFlashcardAnswerRevealClick = { viewModel.revealFlashcard() },
        onReadingPickerAnswerSelected = { viewModel.submitReadingPickerAnswer(it) },
        onNext = { viewModel.next(it) },
        onWordClick = { TODO() },
        onFeedback = {
            mainNavigationState.navigate(
                MainDestination.Feedback(
                    FeedbackTopic.Expression(it.id, FeedbackScreen.VocabPractice)
                )
            )
        },
        navigateBack = { mainNavigationState.navigateBack() },
        finishPractice = { viewModel.finishPractice() }
    )

}