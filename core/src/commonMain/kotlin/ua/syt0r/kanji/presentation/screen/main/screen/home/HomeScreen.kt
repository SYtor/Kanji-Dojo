package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

@Composable
fun HomeScreen(
    mainNavigationState: MainNavigationState,
    viewModel: HomeScreenContract.ViewModel = getMultiplatformViewModel(),
) {

    val homeNavigationState = rememberHomeNavigationState(viewModel.defaultTab)

    val tabContent = remember {
        movableContentOf { HomeNavigationContent(homeNavigationState, mainNavigationState) }
    }

    HomeScreenUI(
        availableTabs = HomeScreenTab.VisibleTabs,
        selectedTabState = homeNavigationState.selectedTab,
        syncIconState = viewModel.syncIconState,
        onTabSelected = { homeNavigationState.navigate(it) },
        onSyncButtonClick = {
            val isSyncStarted = viewModel.trySync()
            if (!isSyncStarted) mainNavigationState.navigate(MainDestination.Sync)
        },
        onSponsorButtonClick = { mainNavigationState.navigate(MainDestination.Sponsor) }
    ) {

        tabContent()

    }

    val analyticsManager = koinInject<AnalyticsManager>()
    LaunchedEffect(Unit) {
        snapshotFlow { homeNavigationState.selectedTab.value }
            .distinctUntilChanged()
            .onEach { analyticsManager.setScreen(it.analyticsName) }
            .launchIn(this)
    }

}