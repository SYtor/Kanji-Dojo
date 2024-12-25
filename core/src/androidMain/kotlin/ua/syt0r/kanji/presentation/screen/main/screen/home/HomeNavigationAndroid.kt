package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
actual fun rememberHomeNavigationState(defaultTab: HomeScreenTab): HomeNavigationState {
    val navController = rememberNavController()

    val tabState = rememberSaveable { mutableStateOf(defaultTab) }

    // Caching current tab value because back stack becomes null for a moment after config change
    // and wrong selected tab is displayed
    LaunchedEffect(Unit) {
        navController.currentBackStackEntryFlow
            .map { it.destination.route?.let { route -> HomeScreenTab.valueOf(route) } }
            .filterNotNull()
            .onEach { tabState.value = it }
            .collect()
    }

    return remember { AndroidHomeNavigationState(defaultTab, navController, tabState) }
}

private val HomeScreenTab.route: String
    get() = this.name

@Stable
private class AndroidHomeNavigationState(
    val defaultTab: HomeScreenTab,
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

    NavHost(
        navController = homeNavigationState.navHostController,
        startDestination = homeNavigationState.defaultTab.route,
        modifier = Modifier.fillMaxSize()
    ) {

        HomeScreenTab.values().forEach { tab ->
            composable(
                route = tab.route,
                content = {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        tab.content(mainNavigationState)
                    }
                }
            )
        }

    }

}