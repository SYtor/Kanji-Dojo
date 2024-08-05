package ua.syt0r.kanji.presentation.screen.main.screen.deck_details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.DeckDetailsScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration

@Composable
fun DeckDetailsScreen(
    configuration: DeckDetailsScreenConfiguration,
    mainNavigationState: MainNavigationState,
    viewModel: DeckDetailsScreenContract.ViewModel = getMultiplatformViewModel(),
) {

    LaunchedEffect(Unit) {
        viewModel.loadData(configuration)
        viewModel.reportScreenShown()
    }

    DeckDetailsScreenUI(
        state = viewModel.state.collectAsState(),
        navigateUp = { mainNavigationState.navigateBack() },
        navigateToDeckEdit = {
            val deckEditConfiguration = when (configuration) {
                is DeckDetailsScreenConfiguration.LetterDeck -> {
                    DeckEditScreenConfiguration.LetterDeck.Edit(
                        title = viewModel.state.value.let { it as ScreenState.Loaded }.title,
                        letterDeckId = configuration.deckId
                    )
                }

                is DeckDetailsScreenConfiguration.VocabDeck -> {
                    DeckEditScreenConfiguration.VocabDeck.Edit(
                        title = viewModel.state.value.let { it as ScreenState.Loaded }.title,
                        vocabDeckId = configuration.deckId
                    )
                }
            }
            mainNavigationState.navigate(MainDestination.DeckEdit(deckEditConfiguration))
        },
        navigateToCharacterDetails = { mainNavigationState.navigate(MainDestination.KanjiInfo(it)) },
        startGroupReview = { group ->
            val configuration = viewModel.getPracticeConfiguration(group)
            mainNavigationState.navigate(configuration)
        },
        startMultiselectReview = {
            val configuration = viewModel.getMultiselectPracticeConfiguration()
            mainNavigationState.navigate(configuration)
        }
    )

}
