package ua.syt0r.kanji.presentation.screen.main.screen.sync

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import ua.syt0r.kanji.core.ApiRequestIssue
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.toLocalDateTime
import ua.syt0r.kanji.core.user_data.preferences.PreferencesSyncDataInfo
import ua.syt0r.kanji.presentation.common.CommonDateTimeFormat
import ua.syt0r.kanji.presentation.common.InvertedButton
import ua.syt0r.kanji.presentation.common.ScrollableScreenContainer
import ua.syt0r.kanji.presentation.common.theme.errorColors
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.snapToBiggerContainerCrossfadeTransitionSpec
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.IndicatorCircle
import ua.syt0r.kanji.presentation.screen.main.screen.sync.SyncScreenContract.ScreenState

@Composable
fun SyncScreenUI(
    state: State<ScreenState>,
    onUpClick: () -> Unit,
    navigateToAccountScreen: () -> Unit,
    sync: () -> Unit
) {

    ScreenContainer(
        state = state,
        onUpClick = onUpClick,
        loading = { FancyLoading(Modifier.fillMaxSize().wrapContentSize()) },
        guide = { screenState ->

            ScrollableScreenContainer(
                contentModifier = Modifier.wrapContentHeight()
            ) {

                Text(
                    text = "Automatically upload your data to the cloud and sync your progress between various devices. To enable: ",
                    textAlign = TextAlign.Justify
                )

                ClickableActionRow(
                    label = "Create account and sign in",
                    isCompleted = false,
                    onClick = navigateToAccountScreen
                )

                ClickableActionRow(
                    label = "Purchase subscription (currently available only from Google Play version)",
                    isCompleted = false,
                    onClick = navigateToAccountScreen
                )

            }

        },
        syncEnabled = { screenState ->
            ScrollableScreenContainer {

                val syncState = screenState.syncState.collectAsState().value
                SyncStatusListItem(syncState)

                if (syncState is SyncState.Error.Api) {
                    SyncErrorListItem(syncState.issue)
                }

                LocalSyncDataListItem(screenState.localDataState.collectAsState().value)

                Spacer(Modifier.weight(1f))

                InvertedButton(
                    onClick = sync,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sync now")
                    Icon(Icons.Default.Sync, null)
                }

            }
        },
        accountError = {
            ScrollableScreenContainer {
                Text("There's an error with your account")
            }
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContainer(
    state: State<ScreenState>,
    onUpClick: () -> Unit,
    loading: @Composable () -> Unit,
    guide: @Composable (screenState: ScreenState.Guide) -> Unit,
    syncEnabled: @Composable (screenState: ScreenState.SyncEnabled) -> Unit,
    accountError: @Composable () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync") },
                navigationIcon = {
                    IconButton(onUpClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.value,
            modifier = Modifier.padding(paddingValues),
            transitionSpec = snapToBiggerContainerCrossfadeTransitionSpec()
        ) { screenState ->

            when (screenState) {
                ScreenState.Loading -> loading()
                is ScreenState.Guide -> guide(screenState)
                is ScreenState.SyncEnabled -> syncEnabled(screenState)
                ScreenState.AccountError -> accountError()
            }

        }

    }

}

@Composable
private fun ClickableActionRow(
    label: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {

        if (isCompleted) {
            Box(
                modifier = Modifier.size(24.dp)
                    .background(MaterialTheme.extraColorScheme.success, CircleShape)
            ) {
                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.surface)
            }
        } else {
            Box(
                modifier = Modifier.size(24.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            )
        }

        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )

        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)

    }
}

@Composable
private fun SyncStatusListItem(syncState: SyncState) {

    val statusMessage: String
    val statusIndicator: @Composable () -> Unit

    when (syncState) {
        SyncState.Refreshing -> {
            statusMessage = "Checking server for updates..."
            statusIndicator = {}
        }

        is SyncState.Conflict -> {
            statusMessage = "Local and remote data differs"
            statusIndicator = {}
        }

        is SyncState.TrackingChanges -> {
            val uploadAvailable = syncState.uploadAvailable.collectAsState().value
            when {
                uploadAvailable -> {
                    statusMessage = "Can upload updated data"
                    statusIndicator = {
                        IndicatorCircle(
                            MaterialTheme.extraColorScheme.due,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                else -> {
                    statusMessage = "Up to date with the server"
                    statusIndicator = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    color = MaterialTheme.extraColorScheme.success,
                                    shape = MaterialTheme.shapes.small
                                ),
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
        }

        is SyncState.Error.Api -> {
            statusMessage = "Error"
            statusIndicator = { IndicatorCircle(MaterialTheme.colorScheme.error) }
        }

        SyncState.Uploading -> {
            statusMessage = "Uploading"
            statusIndicator = {}
        }

        SyncState.Downloading -> {
            statusMessage = "Downloading"
            statusIndicator = {}
        }

        SyncState.Canceled -> {
            statusMessage = "Canceled, click on sync button to restart"
            statusIndicator = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
        }
    }

    ListItem(
        headlineContent = { Text("Status") },
        supportingContent = { Text(statusMessage) },
        trailingContent = statusIndicator
    )
}


@Composable
private fun SyncErrorListItem(issue: ApiRequestIssue) {

    val title: String
    val message: String

    when (issue) {
        ApiRequestIssue.NoConnection -> {
            title = "" // todo reuse strings from account screen
            message = ""
        }

        ApiRequestIssue.NoSubscription -> {
            title = ""
            message = ""
        }

        ApiRequestIssue.NotAuthenticated -> {
            title = ""
            message = ""
        }

        is ApiRequestIssue.Other -> {
            title = ""
            message = ""
        }
    }

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(message) },
        colors = ListItemDefaults.errorColors(),
        modifier = Modifier.clip(MaterialTheme.shapes.medium)
    )

}

@Composable
private fun LocalSyncDataListItem(localData: PreferencesSyncDataInfo) {

    ListItem(
        modifier = Modifier.clip(MaterialTheme.shapes.medium),
        headlineContent = { Text("Current Data") },
        supportingContent = {

            Column {

                Text(text = "ID: ${localData.dataId}", overflow = TextOverflow.Ellipsis)

                val formattedTimestamp = localData.dataTimestamp
                    ?.let { Instant.fromEpochMilliseconds(it) }
                    ?.toLocalDateTime()
                    ?.format(CommonDateTimeFormat)
                    ?: "-"

                Text(text = "Timestamp: $formattedTimestamp")

            }
        }
    )

}
