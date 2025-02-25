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
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenData

@Composable
fun DeckDetailsScreen(
    configuration: DeckDetailsScreenConfiguration,
    mainNavigationState: MainNavigationState,
    viewModel: DeckDetailsScreenContract.ViewModel = getMultiplatformViewModel(),
) {

    LaunchedEffect(Unit) {
        viewModel.loadData(configuration)
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
        navigateToCharacterDetails = {
            val screenData = InfoScreenData.Letter(it)
            mainNavigationState.navigate(MainDestination.Info(screenData))
        },
        navigateToCardDetails = {
            val screenData = it.data.card.data
                .run { InfoScreenData.Vocab(dictionaryId, kanjiReading, kanaReading) }
            mainNavigationState.navigate(MainDestination.Info(screenData))
        },
        startGroupReview = { group ->
            val reviewConfiguration = viewModel.getPracticeConfiguration(group)
            mainNavigationState.navigate(reviewConfiguration)
        },
        startMultiselectReview = {
            val reviewConfiguration = viewModel.getMultiselectPracticeConfiguration()
            mainNavigationState.navigate(reviewConfiguration)
        }
    )

}
