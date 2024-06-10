package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration.LetterDeck
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.LetterDeckDetailsContract.ScreenState

@Composable
fun LetterDeckDetailsScreen(
    deckId: Long,
    mainNavigationState: MainNavigationState,
    viewModel: LetterDeckDetailsContract.ViewModel = getMultiplatformViewModel(),
) {

    LaunchedEffect(Unit) {
        viewModel.notifyScreenShown(deckId)
        viewModel.reportScreenShown()
    }

    LetterDeckDetailsScreenUI(
        state = viewModel.state.collectAsState(),
        updateConfiguration = { viewModel.updateConfiguration(it) },
        navigateUp = { mainNavigationState.navigateBack() },
        navigateToDeckEdit = {
            val configuration = MainDestination.DeckEdit(
                LetterDeck.Edit(
                    title = viewModel.state.value.let { it as ScreenState.Loaded }.title,
                    letterDeckId = deckId
                )
            )
            mainNavigationState.navigate(configuration)
        },
        showGroupDetails = { viewModel.showGroupDetails(it) },
        selectAllClick = { viewModel.selectAll() },
        deselectAllClick = { viewModel.deselectAll() },
        navigateToCharacterDetails = { mainNavigationState.navigate(MainDestination.KanjiInfo(it)) },
        startGroupReview = { group ->
            val configuration = viewModel.getPracticeConfiguration(group)
            mainNavigationState.navigate(configuration)
        },
        leaveSelectionMode = { viewModel.toggleSelectionMode() },
        startSelectionMode = { viewModel.toggleSelectionMode() },
        toggleSelection = { viewModel.toggleSelection(it) },
        startMultiselectReview = {
            val configuration = viewModel.getMultiselectPracticeConfiguration()
            mainNavigationState.navigate(configuration)
        }
    )

}
