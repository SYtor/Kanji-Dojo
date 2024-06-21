package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.home.data.HomeScreenTab

private val SponsorIcon: ImageVector = Icons.Outlined.Handshake

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenUI(
    availableTabs: List<HomeScreenTab>,
    selectedTabState: State<HomeScreenTab>,
    onTabSelected: (HomeScreenTab) -> Unit,
    onSponsorButtonClick: () -> Unit,
    screenTabContent: @Composable () -> Unit
) {

    if (LocalOrientation.current == Orientation.Landscape) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier.fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
                    .width(IntrinsicSize.Max)
            ) {

                Text(
                    text = resolveString { appName },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                availableTabs.forEach { tab ->
                    HorizontalTabButton(
                        tab = tab,
                        selected = tab == selectedTabState.value,
                        onClick = { onTabSelected(tab) }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onSponsorButtonClick) {
                    Icon(SponsorIcon, null)
                }

            }

            Surface { screenTabContent.invoke() }

        }

    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = resolveString { home.screenTitle }) },
                    actions = {
                        IconButton(onClick = onSponsorButtonClick) {
                            Icon(SponsorIcon, null)
                        }
                    }
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

@Composable
private fun HorizontalTabButton(
    tab: HomeScreenTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .let {
                if (selected) it.background(MaterialTheme.colorScheme.surfaceVariant)
                else it
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            tab.iconContent()
        }
        Text(
            text = resolveString(tab.titleResolver),
            style = MaterialTheme.typography.labelLarge
        )
    }
}
