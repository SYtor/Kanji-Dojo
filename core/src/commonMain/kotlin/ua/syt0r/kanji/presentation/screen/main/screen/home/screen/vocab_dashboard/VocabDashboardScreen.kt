package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration.VocabDeck

@Composable
fun VocabDashboardScreen(
    mainNavigationState: MainNavigationState,
    viewModel: VocabDashboardScreenContract.ViewModel = getMultiplatformViewModel()
) {

    VocabDashboardScreenUI(
        screenState = viewModel.screenState.collectAsState(),
        mergeDecks = { viewModel.mergeDecks(it) },
        sortDecks = { viewModel.sortDecks(it) },
        createDeck = {
            mainNavigationState.navigate(
                MainDestination.DeckEdit(VocabDeck.CreateNew)
            )
        },
        navigateToDeckDetails = {
            mainNavigationState.navigate(
                MainDestination.DeckDetails(DeckDetailsScreenConfiguration.VocabDeck(it.id))
            )
        },
        startQuickPractice = { item, studyType, words ->
            mainNavigationState.navigate(MainDestination.VocabPractice(words))
        }
    )

}