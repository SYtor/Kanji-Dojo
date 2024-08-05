package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.common.rememberUrlHandler
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.about.KanjiDojoGithubLink

@Composable
fun GeneralDashboardScreen(
    mainNavigationState: MainNavigationState,
    viewModel: GeneralDashboardScreenContract.ViewModel = getMultiplatformViewModel()
) {

    GeneralDashboardScreenUI(
        state = viewModel.state.collectAsState(),
        navigateToDailyLimitConfiguration = {},
        navigateToCreateLetterDeck = { mainNavigationState.navigate(MainDestination.LetterDeckPicker) },
        navigateToCreateVocabDeck = { mainNavigationState.navigate(MainDestination.LetterDeckPicker) },
        navigateToLetterPractice = { mainNavigationState.navigate(it) },
        navigateToVocabPractice = { mainNavigationState.navigate(it) }
    )

}
