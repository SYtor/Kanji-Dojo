package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import org.koin.java.KoinJavaComponent.getKoin
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.data.HomeScreenTab
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreen

@Composable
actual fun rememberHomeNavigationState(): HomeNavigationState {
    val tabState = rememberSaveable { mutableStateOf<HomeScreenTab>(HomeScreenTab.Default) }
    return rememberSaveable { MultiplatformHomeNavigationState(tabState) }
}

class MultiplatformHomeNavigationState(
    override val selectedTab: MutableState<HomeScreenTab>
) : HomeNavigationState {
    override fun navigate(tab: HomeScreenTab) {
        selectedTab.value = tab
    }
}

@Composable
actual fun HomeNavigationContent(
    homeNavigationState: HomeNavigationState,
    mainNavigationState: MainNavigationState
) {
    homeNavigationState as MultiplatformHomeNavigationState

    val stateHolder = rememberSaveableStateHolder()

    val tab = homeNavigationState.selectedTab.value
    val settingsScreenContent: SettingsScreenContract.Content = remember {
        getKoin().get()
    }

    stateHolder.SaveableStateProvider(tab.name) {
        when (tab) {
            HomeScreenTab.LettersDashboard -> {
                LettersDashboardScreen(
                    mainNavigationState = mainNavigationState,
                    viewModel = getMultiplatformViewModel()
                )
            }

            HomeScreenTab.VocabDashboard -> {
                VocabDashboardScreen(mainNavigationState = mainNavigationState)
            }

            HomeScreenTab.Stats -> {
                StatsScreen(viewModel = getMultiplatformViewModel())
            }

            HomeScreenTab.Search -> {
                SearchScreen(
                    mainNavigationState = mainNavigationState,
                    viewModel = getMultiplatformViewModel()
                )
            }

            HomeScreenTab.Settings -> {
                settingsScreenContent.Draw(mainNavigationState)
            }
        }
    }


}