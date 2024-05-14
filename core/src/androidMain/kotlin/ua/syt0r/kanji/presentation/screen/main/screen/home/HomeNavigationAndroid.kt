package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.get
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.data.HomeScreenTab
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.PracticeDashboardScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreen

@Composable
actual fun rememberHomeNavigationState(): HomeNavigationState {
    val navController = rememberNavController()

    val tabState = rememberSaveable { mutableStateOf(HomeScreenTab.Default) }

    // Caching current tab value because back stack becomes null for a moment after config change
    // and wrong selected tab is displayed
    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow
            .map { it.destination.route?.let { route -> HomeScreenTab.valueOf(route) } }
            .filterNotNull()
            .onEach { tabState.value = it }
            .collect()
    }

    return remember { AndroidHomeNavigationState(navController, tabState) }
}

private val HomeScreenTab.route: String
    get() = this.name

@Stable
private class AndroidHomeNavigationState(
    val navHostController: NavHostController,
    tabState: State<HomeScreenTab>
) : HomeNavigationState {

    override val selectedTab: State<HomeScreenTab> = tabState

    override fun navigate(tab: HomeScreenTab) {
        navHostController.navigate(tab.route) {
            popUpTo(navHostController.currentDestination!!.route!!) {
                inclusive = true
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

}

@Composable
actual fun HomeNavigationContent(
    homeNavigationState: HomeNavigationState,
    mainNavigationState: MainNavigationState
) {

    homeNavigationState as AndroidHomeNavigationState

    val settingsScreenContent = get<SettingsScreenContract.Content>()

    NavHost(
        navController = homeNavigationState.navHostController,
        startDestination = HomeScreenTab.Default.route
    ) {

        composable(
            route = HomeScreenTab.PracticeDashboard.route,
            content = {
                PracticeDashboardScreen(
                    mainNavigationState = mainNavigationState,
                    viewModel = getMultiplatformViewModel()
                )
            }
        )

        composable(
            route = HomeScreenTab.Stats.route,
            content = {
                StatsScreen(
                    viewModel = getMultiplatformViewModel()
                )
            }
        )

        composable(
            route = HomeScreenTab.VocabDashboard.route,
            content = {
                VocabDashboardScreen(mainNavigationState = mainNavigationState)
            }
        )

        composable(
            route = HomeScreenTab.Search.route,
            content = {
                SearchScreen(
                    mainNavigationState = mainNavigationState,
                    viewModel = getMultiplatformViewModel()
                )
            }
        )

        composable(
            route = HomeScreenTab.Settings.route,
            content = {
                settingsScreenContent.Draw(
                    mainNavigationState = mainNavigationState
                )
            }
        )

    }

}