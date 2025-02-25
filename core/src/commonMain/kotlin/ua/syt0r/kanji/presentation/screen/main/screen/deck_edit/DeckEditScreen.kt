package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenData

@Composable
fun DeckEditScreen(
    configuration: DeckEditScreenConfiguration,
    mainNavigationState: MainNavigationState,
    viewModel: DeckEditScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.initialize(configuration)
    }

    DeckEditScreenUI(
        configuration = configuration,
        state = viewModel.state.collectAsState(),
        navigateBack = { mainNavigationState.navigateBack() },
        submitSearch = { viewModel.searchCharacters(it) },
        dismissSearchResult = { viewModel.dismissSearchResult() },
        onItemClick = {

            val screenData = when (it) {
                is LetterDeckEditListItem -> InfoScreenData.Letter(it.character)
                is VocabDeckEditListItem -> {
                    val cardData = it.displayCardData.value
                    InfoScreenData.Vocab(
                        id = cardData.dictionaryId,
                        kanjiReading = cardData.kanjiReading,
                        kanaReading = cardData.kanaReading
                    )
                }
            }
            mainNavigationState.navigate(MainDestination.Info(screenData))
        },
        toggleRemoval = { viewModel.toggleRemoval(it) },
        saveChanges = { viewModel.saveDeck() },
        deleteDeck = { viewModel.deleteDeck() },
        onCompleted = {
            when {
                configuration is DeckEditScreenConfiguration.EditExisting && !it.wasDeleted -> {
                    mainNavigationState.navigateBack()
                }

                else -> mainNavigationState.popUpToHome()
            }
        }
    )

}
