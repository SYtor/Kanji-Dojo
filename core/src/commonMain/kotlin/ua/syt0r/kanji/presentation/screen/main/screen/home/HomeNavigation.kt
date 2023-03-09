package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.data.HomeScreenTab

interface HomeNavigationState {
    val selectedTab: State<HomeScreenTab>
    fun navigate(tab: HomeScreenTab)
}

@Composable
expect fun rememberHomeNavigationState(): HomeNavigationState

@Composable
expect fun HomeNavigationContent(
    homeNavigationState: HomeNavigationState,
    mainNavigationState: MainNavigationState
)
