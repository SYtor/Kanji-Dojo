package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.screen.main.screen.home.HomeScreenUI
import ua.syt0r.kanji.presentation.screen.main.screen.home.data.HomeScreenTab

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview(
    content: @Composable () -> Unit = { }
) {
    AppTheme {
        HomeScreenUI(
            availableTabs = HomeScreenTab.values().toList(),
            syncState = rememberUpdatedState(SyncState.Disabled),
            selectedTabState = HomeScreenTab.values().first().run { rememberUpdatedState(this) },
            onTabSelected = {},
            onSyncButtonClick = {},
            onSponsorButtonClick = {},
            screenTabContent = { content() }
        )
    }
}

@Preview(showSystemUi = true, device = Devices.PIXEL_C)
@Composable
private fun TabletPreview() {
    HomeScreenPreview()
}