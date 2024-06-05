package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
fun DeckEditScreen(
    configuration: DeckEditScreenConfiguration,
    mainNavigationState: MainNavigationState,
    viewModel: DeckEditScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.initialize(configuration)
        viewModel.reportScreenShown()
    }

    DeckEditScreenUI(
        configuration = configuration,
        state = viewModel.state.collectAsState(),
        navigateBack = { mainNavigationState.navigateBack() },
        submitSearch = { viewModel.searchCharacters(it) },
        onCharacterInfoClick = { mainNavigationState.navigate(MainDestination.KanjiInfo(it)) },
        toggleRemoval = { viewModel.toggleRemoval(it) },
        saveChanges = { viewModel.saveDeck() },
        deleteDeck = { viewModel.deleteDeck() },
        onCompleted = { mainNavigationState.popUpToHome() }
    )

}
