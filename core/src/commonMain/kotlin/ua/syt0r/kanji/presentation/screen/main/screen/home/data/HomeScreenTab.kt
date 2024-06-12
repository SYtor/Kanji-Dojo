package ua.syt0r.kanji.presentation.screen.main.screen.home.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope

enum class HomeScreenTab(
    val iconContent: @Composable () -> Unit,
    val titleResolver: StringResolveScope<String>
) {

    LettersDashboard(
        iconContent = { Text(text = "字", style = MaterialTheme.typography.titleMedium) },
        titleResolver = { home.lettersDashboardTabLabel },
    ),
    VocabDashboard(
        iconContent = { Text(text = "語", style = MaterialTheme.typography.titleMedium) },
        titleResolver = { home.vocabDashboardTabLabel }
    ),
    Stats(
        iconContent = { Icon(Icons.Default.QueryStats, null) },
        titleResolver = { home.statsTabLabel }
    ),
    Search(
        iconContent = { Icon(Icons.Default.Search, null) },
        titleResolver = { home.searchTabLabel }
    ),
    Settings(
        iconContent = { Icon(Icons.Outlined.Settings, null) },
        titleResolver = { home.settingsTabLabel }
    );

    companion object {
        val Default = LettersDashboard
        val VisibleTabs: List<HomeScreenTab> = HomeScreenTab.values().toList()
    }

}