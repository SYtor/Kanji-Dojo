package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
fun LettersDashboardScreen(
    mainNavigationState: MainNavigationState,
    viewModel: LettersDashboardScreenContract.ViewModel
) {

    LaunchedEffect(Unit) {
        viewModel.reportScreenShown()
    }

    LettersDashboardScreenUI(
        state = viewModel.state,
        startMerge = { viewModel.enablePracticeMergeMode() },
        merge = { viewModel.merge(it) },
        startReorder = { viewModel.enablePracticeReorderMode() },
        reorder = { viewModel.reorder(it) },
        enableDefaultMode = { viewModel.enableDefaultMode() },
        navigateToDeckDetails = {
            mainNavigationState.navigate(MainDestination.LetterDeckDetails(it.deckId))
        },
        startQuickPractice = {
            mainNavigationState.navigate(it)
        },
        updateDailyGoalConfiguration = {
            viewModel.updateDailyGoal(it)
        },
        navigateToDeckPicker = {
            mainNavigationState.navigate(MainDestination.LetterDeckPicker)
        }
    )

}
