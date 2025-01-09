package ua.syt0r.kanji.presentation.screen.main

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import io.ktor.http.Url
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.presentation.common.resources.string.SyncSnackbarStrings
import ua.syt0r.kanji.presentation.common.resources.string.getStrings
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.account.AccountScreenContract

@Composable
fun MainScreen(
    deepLinkHandler: DeepLinkHandler
) {

    val viewModel = getMultiplatformViewModel<MainContract.ViewModel>()
    val navigationState = rememberMainNavigationState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { SyncErrorSnackbar(it) }
            )
        }
    ) {
        MainNavigation(navigationState)
    }

    SyncDialog(
        state = viewModel.syncDialogState.collectAsState(),
        cancelSync = viewModel::cancelSync,
        resolveConflict = viewModel::resolveSyncConflict,
        navigateToAccount = {
            viewModel.cancelSync()
            navigationState.navigate(MainDestination.Account())
        }
    )

    HandleDeepLinksLaunchedEffect(
        deepLinkHandler = deepLinkHandler,
        navigationState = navigationState
    )

    HandleScreenReportsLaunchedEffect(
        navigationState = navigationState
    )

    HandleSyncErrorSnackbarsLaunchedEffect(
        snackbarHostState = snackbarHostState,
        syncDialogState = viewModel.syncDialogState,
        navigationState = navigationState
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

@Composable
private fun HandleSyncErrorSnackbarsLaunchedEffect(
    snackbarHostState: SnackbarHostState,
    syncDialogState: StateFlow<SyncDialogState>,
    navigationState: MainNavigationState
) {

    LaunchedEffect(Unit) {

        syncDialogState.filterIsInstance<SyncDialogState.Error>()
            .filterNot { _ ->
                val destination = navigationState.currentDestination.value
                destination is MainDestination.Account || destination is MainDestination.Sync
            }
            .collectLatest { currentState ->
                if (currentState.showDialog.value)
                    return@collectLatest
                val strings = getStrings().syncSnackbar
                val result = snackbarHostState.showSnackbar(
                    message = currentState.snackbarMessage(strings),
                    actionLabel = strings.actionButton,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long
                )

                if (result == SnackbarResult.ActionPerformed) {
                    currentState.showDialog.value = true
                }
            }
    }

}

private fun SyncDialogState.Error.snackbarMessage(strings: SyncSnackbarStrings): String {
    val extraMessage = when (this) {
        is SyncDialogState.Error.Api -> {
            when (issue) {
                ApiRequestIssue.NoConnection -> strings.errorNoConnection
                ApiRequestIssue.NoSubscription -> strings.errorNoSubscription
                ApiRequestIssue.NotAuthenticated -> strings.errorNotAuthenticated
                is ApiRequestIssue.Other -> null
            }
        }

        is SyncDialogState.Error.Unsupported -> strings.errorDataNotSupported
    }
    return when {
        extraMessage != null -> strings.errorMessageTemplate.format(extraMessage)
        else -> strings.errorMessageNoReason
    }
}


@Composable
private fun SyncErrorSnackbar(snackbarData: SnackbarData) {
    Snackbar(
        snackbarData = snackbarData,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        actionColor = MaterialTheme.colorScheme.onErrorContainer,
        actionContentColor = MaterialTheme.colorScheme.onErrorContainer,
        dismissActionContentColor = MaterialTheme.colorScheme.onErrorContainer
    )
}
