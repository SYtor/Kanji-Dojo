package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
fun VocabPracticeScreen(
    expressionsIds: List<Long>,
    mainNavigationState: MainNavigationState,
    viewModel: VocabPracticeScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.initialize(expressionsIds)
        viewModel.reportScreenShown()
    }

    VocabPracticeScreenUI(
        state = viewModel.state.collectAsState(),
        onConfigured = { viewModel.configure(it) },
        onAnswerSelected = { viewModel.submitAnswer(it) },
        onNext = { viewModel.next() },
        navigateBack = { mainNavigationState.navigateBack() }
    )

}