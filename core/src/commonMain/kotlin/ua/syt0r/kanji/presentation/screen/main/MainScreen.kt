package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.getMultiplatformViewModel

@Composable
fun MainScreen() {

    val viewModel = getMultiplatformViewModel<MainContract.ViewModel>()

    val syncState = viewModel.syncState.collectAsState()

    when (syncState.value) {
        SyncState.Syncing,
        SyncState.Fail -> {
            SyncDialog(
                state = syncState,
                onCancelRequest = viewModel::cancelSync
            )
        }

        else -> {}
    }

    val navigationState = rememberMainNavigationState()
    MainNavigation(navigationState)

    val analyticsManager = koinInject<AnalyticsManager>()
    LaunchedEffect(Unit) {
        snapshotFlow { navigationState.currentDestination.value }
            .map { it?.analyticsName }
            .filterNotNull()
            .onEach { analyticsManager.setScreen(it) }
            .launchIn(this)
    }

}

@Composable
private fun SyncDialog(
    state: State<SyncState>,
    onCancelRequest: () -> Unit
) {
    MultiplatformDialog(
        onDismissRequest = {},
        title = {
            Text(text = "Sync")
        },
        content = {
            CircularProgressIndicator(Modifier.fillMaxWidth().wrapContentSize())
        },
        buttons = {
            TextButton(
                onClick = onCancelRequest
            ) { Text("Cancel") }
        }
    )
}