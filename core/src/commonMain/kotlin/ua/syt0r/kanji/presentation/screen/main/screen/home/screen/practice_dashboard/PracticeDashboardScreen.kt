package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration.LetterDeck
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.ui.PracticeDashboardScreenUI

@Composable
fun PracticeDashboardScreen(
    mainNavigationState: MainNavigationState,
    viewModel: PracticeDashboardScreenContract.ViewModel
) {

    LaunchedEffect(Unit) {
        viewModel.notifyScreenShown()
        viewModel.reportScreenShown()
    }

    PracticeDashboardScreenUI(
        state = viewModel.state,
        startMerge = { viewModel.enablePracticeMergeMode() },
        merge = { viewModel.merge(it) },
        startReorder = { viewModel.enablePracticeReorderMode() },
        reorder = { viewModel.reorder(it) },
        enableDefaultMode = { viewModel.enableDefaultMode() },
        navigateToPracticeDetails = {
            mainNavigationState.navigate(MainDestination.LetterDeckDetails(it.practiceId))
        },
        startQuickPractice = {
            mainNavigationState.navigate(it)
        },
        updateDailyGoalConfiguration = {
            viewModel.updateDailyGoal(it)
        },
        navigateToImportPractice = {
            mainNavigationState.navigate(MainDestination.ImportPractice)
        },
        navigateToCreatePractice = {
            mainNavigationState.navigate(MainDestination.DeckEdit(LetterDeck.CreateNew))
        },
    )

}
