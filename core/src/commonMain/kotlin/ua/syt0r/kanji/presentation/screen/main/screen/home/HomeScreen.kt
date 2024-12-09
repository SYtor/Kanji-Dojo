package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState

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
        syncIconState = viewModel.syncIconState,
        onTabSelected = { homeNavigationState.navigate(it) },
        onSyncButtonClick = viewModel::sync,
        onSponsorButtonClick = { mainNavigationState.value.navigate(MainDestination.Sponsor) }
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