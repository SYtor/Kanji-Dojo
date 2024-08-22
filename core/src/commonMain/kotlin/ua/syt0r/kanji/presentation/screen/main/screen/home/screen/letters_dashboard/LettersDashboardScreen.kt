package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import androidx.compose.runtime.Composable
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration

@Composable
fun LettersDashboardScreen(
    mainNavigationState: MainNavigationState,
    viewModel: LettersDashboardScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LettersDashboardScreenUI(
        state = viewModel.state,
        mergeDecks = { viewModel.mergeDecks(it) },
        sortDecks = { viewModel.sortDecks(it) },
        navigateToDeckDetails = {
            val configuration = DeckDetailsScreenConfiguration.LetterDeck(it.deckId)
            mainNavigationState.navigate(MainDestination.DeckDetails(configuration))
        },
        startQuickPractice = { item, practiceType, letters ->
            val destination: MainDestination.LetterPractice = MainDestination.LetterPractice(
                configuration = LetterPracticeScreenConfiguration(
                    letters.associateWith { item.deckId },
                    practiceType = practiceType
                )
            )
            mainNavigationState.navigate(destination)
        },
        navigateToDailyLimit = {
            mainNavigationState.navigate(MainDestination.DailyLimit)
        },
        navigateToDeckPicker = {
            val destination = MainDestination.DeckPicker(DeckPickerScreenConfiguration.Letters)
            mainNavigationState.navigate(destination)
        }
    )

}
