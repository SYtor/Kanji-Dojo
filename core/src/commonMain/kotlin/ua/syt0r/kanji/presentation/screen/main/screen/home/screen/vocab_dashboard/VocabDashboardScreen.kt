package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import ua.syt0r.kanji.presentation.common.resources.string.getStrings
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration.VocabDeck

@Composable
fun VocabDashboardScreen(
    mainNavigationState: MainNavigationState,
    viewModel: VocabDashboardScreenContract.ViewModel = getMultiplatformViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.invalidate()
        viewModel.reportScreenShown()
    }

    VocabDashboardScreenUI(
        state = viewModel.state.collectAsState(),
        select = { viewModel.select(it) },
        createDeck = {
            mainNavigationState.navigate(
                MainDestination.DeckEdit(VocabDeck.CreateNew)
            )
        },
        onEditClick = { deck ->
            val title = deck.titleResolver(getStrings())
            val configuration = deck.id
                ?.let { VocabDeck.Edit(title, it) }
                ?: VocabDeck.CreateDerived(title, deck.expressionIds)
            mainNavigationState.navigate(MainDestination.DeckEdit(configuration))
        },
        navigateToPractice = {
            mainNavigationState.navigate(
                MainDestination.VocabPractice(wordIds = it.expressionIds)
            )
        }
    )

}