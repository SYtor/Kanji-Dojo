package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.java.KoinJavaComponent.getKoin
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.VersionChangeDialog

@Composable
fun MainScreen() {

    val navigationState = rememberMainNavigationState()
    MainNavigation(navigationState)

    val analyticsManager = remember { getKoin().get<AnalyticsManager>() }
    LaunchedEffect(Unit) {
        snapshotFlow { navigationState.currentDestination.value }
            .map { it?.analyticsName }
            .filterNotNull()
            .onEach { analyticsManager.setScreen(it) }
            .launchIn(this)
    }

    val viewModel = getMultiplatformViewModel<MainContract.ViewModel>()
    if (viewModel.shouldShowVersionChangeDialog.value) {
        VersionChangeDialog { viewModel.markVersionChangeDialogShown() }
    }

}