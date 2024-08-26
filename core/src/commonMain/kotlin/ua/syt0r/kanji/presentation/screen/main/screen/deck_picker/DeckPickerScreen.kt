package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.common.rememberUrlHandler
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerScreenConfiguration


@Composable
fun DeckPickerScreen(
    configuration: DeckPickerScreenConfiguration,
    mainNavigationState: MainNavigationState,
    viewModel: DeckPickerScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.loadData(configuration)
    }

    val urlHandler = rememberUrlHandler()

    DeckPickerScreenUI(
        state = viewModel.state.collectAsState(),
        onUpButtonClick = { mainNavigationState.navigateBack() },
        createEmpty = {
            val deckEditConfiguration = when (configuration) {
                DeckPickerScreenConfiguration.Letters -> DeckEditScreenConfiguration.LetterDeck.CreateNew
                DeckPickerScreenConfiguration.Vocab -> DeckEditScreenConfiguration.VocabDeck.CreateNew
            }
            val destination = MainDestination.DeckEdit(deckEditConfiguration)
            mainNavigationState.navigate(destination)
        },
        onLetterDeckClick = { classification, title ->
            val destination = MainDestination.DeckEdit(
                DeckEditScreenConfiguration.LetterDeck.CreateDerived(
                    title = title,
                    classification = classification
                )
            )
            mainNavigationState.navigate(destination)
        },
        onVocabDeckClick = { classification, title ->
            val destination = MainDestination.DeckEdit(
                DeckEditScreenConfiguration.VocabDeck.CreateDerived(
                    title = title,
                    classification = classification
                )
            )
            mainNavigationState.navigate(destination)
        },
        onLinkClick = { url -> urlHandler.openInBrowser(url) }
    )

}
