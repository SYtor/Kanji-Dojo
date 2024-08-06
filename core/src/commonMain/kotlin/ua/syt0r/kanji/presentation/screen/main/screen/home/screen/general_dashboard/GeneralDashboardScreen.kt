package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerScreenConfiguration

@Composable
fun GeneralDashboardScreen(
    mainNavigationState: MainNavigationState,
    viewModel: GeneralDashboardScreenContract.ViewModel = getMultiplatformViewModel()
) {

    GeneralDashboardScreenUI(
        state = viewModel.state.collectAsState(),
        navigateToDailyLimitConfiguration = {
            mainNavigationState.navigate(MainDestination.DailyLimit)
        },
        navigateToCreateLetterDeck = {
            val destination = MainDestination.DeckPicker(DeckPickerScreenConfiguration.Letters)
            mainNavigationState.navigate(destination)
        },
        navigateToCreateVocabDeck = {
            val destination = MainDestination.DeckPicker(DeckPickerScreenConfiguration.Vocab)
            mainNavigationState.navigate(destination)
        },
        navigateToLetterPractice = { mainNavigationState.navigate(it) },
        navigateToVocabPractice = { mainNavigationState.navigate(it) }
    )

}
