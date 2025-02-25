package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration

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
            val destination = MainDestination.DeckPicker(DeckPickerScreenConfiguration.Vocab)
            mainNavigationState.navigate(destination)
        },
        navigateToDeckDetails = {
            val configuration = DeckDetailsScreenConfiguration.VocabDeck(it.deckId)
            mainNavigationState.navigate(MainDestination.DeckDetails(configuration))
        },
        navigateToDeckEdit = {
            val configuration = DeckEditScreenConfiguration.VocabDeck.Edit(it.title, it.deckId)
            mainNavigationState.navigate(MainDestination.DeckEdit(configuration))
        },
        startQuickPractice = { item, practiceType, words ->
            val configuration = VocabPracticeScreenConfiguration(
                wordIdToDeckIdMap = words.associateWith { item.deckId },
                practiceType = practiceType
            )
            mainNavigationState.navigate(MainDestination.VocabPractice(configuration))
        }
    )

}