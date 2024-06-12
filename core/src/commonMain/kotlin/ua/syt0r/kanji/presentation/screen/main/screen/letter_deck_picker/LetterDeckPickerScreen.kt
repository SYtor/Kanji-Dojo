package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ua.syt0r.kanji.presentation.common.rememberUrlHandler
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration


@Composable
fun LetterDeckPickerScreen(
    mainNavigationState: MainNavigationState,
    viewModel: LetterDeckPickerScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LaunchedEffect(Unit) { viewModel.reportScreenShown() }

    val urlHandler = rememberUrlHandler()

    LetterDeckPickerScreenUI(
        state = viewModel.state,
        onUpButtonClick = { mainNavigationState.navigateBack() },
        createEmpty = {
            val destination = MainDestination.DeckEdit(
                configuration = DeckEditScreenConfiguration.LetterDeck.CreateNew
            )
            mainNavigationState.navigate(destination)
        },
        onItemSelected = { classification, title ->
            val destination = MainDestination.DeckEdit(
                DeckEditScreenConfiguration.LetterDeck.CreateDerived(
                    title = title,
                    classification = classification
                )
            )
            mainNavigationState.navigate(destination)
        },
        onLinkClick = { url -> urlHandler.openInBrowser(url) }
    )

}
