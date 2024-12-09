package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import io.ktor.http.Url
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.sync.SyncConflictResolveStrategy
import ua.syt0r.kanji.core.sync.SyncFeatureState
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.account.AccountScreenContract

@Composable
fun MainScreen(
    deepLinkHandler: DeepLinkHandler
) {

    val viewModel = getMultiplatformViewModel<MainContract.ViewModel>()

    val syncState = viewModel.syncFeatureState.collectAsState()
    SyncDialog(
        state = syncState,
        onCancelRequest = viewModel::cancelSync,
        resolveConflict = viewModel::resolveConflict
    )

    val navigationState = rememberMainNavigationState()
    MainNavigation(navigationState)

    HandleDeepLinksLaunchedEffect(deepLinkHandler, navigationState)
    HandleScreenReportsLaunchedEffect(navigationState)

}

@Composable
private fun SyncDialog(
    state: State<SyncFeatureState>,
    onCancelRequest: () -> Unit,
    resolveConflict: (SyncConflictResolveStrategy) -> Unit
) {

    val currentState = remember { derivedStateOf { state.value } }.value

    val dialogContent: @Composable ColumnScope.() -> Unit
    val dialogButtons: @Composable RowScope.() -> Unit

    when (currentState) {
        SyncFeatureState.Disabled,
        SyncFeatureState.Loading -> return

        SyncFeatureState.Error -> {
            dialogContent = {
                Text("Error... $currentState")
            }
            dialogButtons = {
                TextButton(
                    onClick = onCancelRequest
                ) { Text("Cancel") }
            }
        }

        is SyncFeatureState.Enabled -> {

            when (val activeState = currentState.state.value) {
                SyncState.Refreshing,
                SyncState.Canceled,
                is SyncState.TrackingChanges -> return

                SyncState.Uploading -> {
                    dialogContent = {
                        Text("Uploading...")
                    }
                    dialogButtons = {
                        TextButton(
                            onClick = onCancelRequest
                        ) { Text("Cancel") }
                    }
                }

                SyncState.Downloading -> {
                    dialogContent = {
                        Text("Downloading...")
                    }
                    dialogButtons = {
                        TextButton(
                            onClick = onCancelRequest
                        ) { Text("Cancel") }
                    }
                }

                is SyncState.Conflict -> {
                    dialogContent = {
                        Text("Conflict \n $activeState")
                    }
                    dialogButtons = {
                        TextButton(
                            onClick = {
                                resolveConflict(SyncConflictResolveStrategy.UploadLocal)
                            }
                        ) { Text("Upload") }

                        TextButton(
                            onClick = {
                                resolveConflict(SyncConflictResolveStrategy.DownloadRemote)
                            }
                        ) { Text("Download") }
                    }
                }

                is SyncState.Error -> {
                    dialogContent = {
                        Text("Error... $activeState")
                    }
                    dialogButtons = {
                        TextButton(
                            onClick = onCancelRequest
                        ) { Text("Cancel") }
                    }
                }
            }

        }
    }

    MultiplatformDialog(
        onDismissRequest = {},
        title = { Text(text = "Sync") },
        content = dialogContent,
        buttons = dialogButtons
    )

}

@Composable
private fun HandleDeepLinksLaunchedEffect(
    deepLinkHandler: DeepLinkHandler,
    navigationState: MainNavigationState
) {

    LaunchedEffect(Unit) {
        deepLinkHandler.deepLinksFlow
            .mapNotNull {
                when {
                    it.startsWith("kanji-dojo://signin") -> {
                        val data = Url(it).parameters.run {
                            AccountScreenContract.ScreenData(
                                refreshToken = get("refreshToken") ?: return@run null,
                                idToken = get("idToken") ?: return@run null
                            )
                        }
                        if (data != null) {
                            MainDestination.Account(data)
                        } else {
                            Logger.d("Couldn't parse deep link $it")
                            null
                        }
                    }

                    else -> {
                        Logger.d("Unsupported deeplink[$it]")
                        null
                    }
                }
            }
            .collectLatest {
                Logger.d("Navigating from deep link to destination[$it]")
                navigationState.navigateToTop(it)
            }
    }

}

@Composable
private fun HandleScreenReportsLaunchedEffect(navigationState: MainNavigationState) {
    val analyticsManager = koinInject<AnalyticsManager>()
    LaunchedEffect(Unit) {
        snapshotFlow { navigationState.currentDestination.value }
            .map { it?.analyticsName }
            .filterNotNull()
            .onEach { analyticsManager.setScreen(it) }
            .launchIn(this)
    }
}
