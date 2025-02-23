package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search

import androidx.compose.runtime.Composable
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackScreen
import ua.syt0r.kanji.presentation.screen.main.screen.feedback.FeedbackTopic
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.ui.SearchScreenUI
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenData
import ua.syt0r.kanji.presentation.screen.main.screen.info.toInfoScreenData

@Composable
fun SearchScreen(
    mainNavigationState: MainNavigationState,
    viewModel: SearchScreenContract.ViewModel = getMultiplatformViewModel()
) {

    SearchScreenUI(
        state = viewModel.state,
        radicalsState = viewModel.radicalsState,
        onSubmitInput = { viewModel.search(it) },
        onRadicalsSectionExpanded = { viewModel.loadRadicalsData() },
        onRadicalsSelected = { viewModel.radicalsSearch(it) },
        onCharacterClick = {
            val screenData = InfoScreenData.Letter(it)
            mainNavigationState.navigate(MainDestination.Info(screenData))
        },
        onWordClick = {
            val screenData = it.toInfoScreenData()
            mainNavigationState.navigate(MainDestination.Info(screenData))
        },
        onScrolledToEnd = { viewModel.loadMoreWords() },
        onWordFeedback = {
            val feedbackTopic = FeedbackTopic.Expression(it.id, FeedbackScreen.Search)
            val destination = MainDestination.Feedback(feedbackTopic)
            mainNavigationState.navigate(destination)
        }
    )

}
