package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.home.data.HomeScreenTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenUI(
    availableTabs: List<HomeScreenTab>,
    selectedTabState: State<HomeScreenTab>,
    onTabSelected: (HomeScreenTab) -> Unit,
    screenTabContent: @Composable () -> Unit
) {

    if (LocalOrientation.current == Orientation.Landscape) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            NavigationRail(
                modifier = Modifier.fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.weight(1f))
                availableTabs.forEach { tab ->
                    NavigationRailItem(
                        selected = tab == selectedTabState.value,
                        onClick = { onTabSelected(tab) },
                        icon = { tab.iconContent() },
                        label = { Text(text = resolveString(tab.titleResolver)) }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Surface { screenTabContent.invoke() }

        }

    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = resolveString { home.screenTitle }) }
                )
            },
            bottomBar = {
                NavigationBar(tonalElevation = 0.dp) {
                    availableTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = tab == selectedTabState.value,
                            onClick = { onTabSelected(tab) },
                            icon = { tab.iconContent() },
                            label = { Text(text = resolveString(tab.titleResolver)) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White)
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                screenTabContent.invoke()
            }

        }

    }

}
