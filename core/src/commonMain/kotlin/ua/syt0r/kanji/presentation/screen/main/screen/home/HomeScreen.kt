package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.movableContentOf
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.data.HomeScreenTab

// TODO With compose 1.5 HomeNavigationContent inside of movableContentOf flickers when passing MainNavigationState directly without State wrapper even with class marked as immutable. Make prettier later
@Composable
fun HomeScreen(
    mainNavigationState: State<MainNavigationState>,
    homeNavigationState: HomeNavigationState = rememberHomeNavigationState(),
    viewModel: HomeScreenContract.ViewModel = getMultiplatformViewModel(),
) {

    val tabContent = movableContentOf {
        HomeNavigationContent(homeNavigationState, mainNavigationState.value)
    }

    HomeScreenUI(
        availableTabs = HomeScreenTab.VisibleTabs,
        selectedTabState = homeNavigationState.selectedTab,
        onTabSelected = { homeNavigationState.navigate(it) },
        onSponsorButtonClick = { mainNavigationState.value.navigate(MainDestination.Sponsor) }
    ) {

        tabContent()

    }

}