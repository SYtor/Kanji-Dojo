package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.text.font.FontWeight
import org.koin.compose.koinInject
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.HomeOutline
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.SearchScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreen
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreen

enum class HomeScreenTab(
    val analyticsName: String,
    val iconContent: @Composable () -> Unit,
    val titleResolver: StringResolveScope<String>,
    val content: @Composable (MainNavigationState) -> Unit
) {

    GeneralDashboard(
        analyticsName = "general_dashboard",
        iconContent = { Icon(ExtraIcons.HomeOutline, null) },
        titleResolver = { home.generalDashboardTabLabel },
        content = { GeneralDashboardScreen(it) }
    ),
    LettersDashboard(
        analyticsName = "letters_dashboard",
        iconContent = {
            Text(
                text = "字",
                fontSize = 18.textDp,
                fontWeight = FontWeight.Bold
            )
        },
        titleResolver = { home.lettersDashboardTabLabel },
        content = { LettersDashboardScreen(mainNavigationState = it) }
    ),
    VocabDashboard(
        analyticsName = "vocab_dashboard",
        iconContent = {
            Text(
                text = "語",
                fontSize = 18.textDp,
                fontWeight = FontWeight.Bold
            )
        },
        titleResolver = { home.vocabDashboardTabLabel },
        content = { VocabDashboardScreen(mainNavigationState = it) }
    ),
    Stats(
        analyticsName = "stats",
        iconContent = { Icon(Icons.Default.QueryStats, null) },
        titleResolver = { home.statsTabLabel },
        content = { StatsScreen() }
    ),
    Search(
        analyticsName = "search",
        iconContent = { Icon(Icons.Default.Search, null) },
        titleResolver = { home.searchTabLabel },
        content = { SearchScreen(mainNavigationState = it) }
    ),
    Settings(
        analyticsName = "settings",
        iconContent = { Icon(Icons.Outlined.Settings, null) },
        titleResolver = { home.settingsTabLabel },
        content = {
            val settingsScreenContent = koinInject<SettingsScreenContract.Content>()
            settingsScreenContent.Draw(mainNavigationState = it)
        }
    );

    companion object {
        val Default = GeneralDashboard
        val VisibleTabs: List<HomeScreenTab> = HomeScreenTab.values().toList()
    }

}

data class SyncIconState(
    val loading: State<Boolean>,
    val indicator: State<SyncIconIndicator>
)

enum class SyncIconIndicator { None, PendingUpload, Completed, Canceled, Error }
