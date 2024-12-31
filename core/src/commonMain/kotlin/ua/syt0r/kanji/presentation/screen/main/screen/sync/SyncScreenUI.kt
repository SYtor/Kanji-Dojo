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
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
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
    sync: () -> Unit,
) {

    val strings = resolveString { this.sync }

    ScreenContainer(
        state = state,
        onUpClick = onUpClick,
        loading = { FancyLoading(Modifier.fillMaxSize().wrapContentSize()) },
        guide = { screenState ->

            ScrollableScreenContainer(
                contentModifier = Modifier.wrapContentHeight()
            ) {

                Text(
                    text = strings.guideMessage,
                    textAlign = TextAlign.Justify
                )

                ClickableActionRow(
                    label = strings.createAccountLabel,
                    isCompleted = false,
                    onClick = navigateToAccountScreen
                )

                ClickableActionRow(
                    label = strings.purchaseSubscriptionLabel,
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

                LocalSyncDataListItem(
                    localData = screenState.localDataState.collectAsState().value
                )

                Spacer(Modifier.weight(1f))

                InvertedButton(
                    onClick = sync,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.syncButton)
                    Icon(Icons.Default.Sync, null)
                }

            }
        },
        accountError = {
            ScrollableScreenContainer {
                Text(strings.accountErrorMessage)
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
                title = { Text(resolveString { sync.title }) },
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
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.surface
                )
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

    val strings = resolveString { sync }
    val statusMessage: String
    val statusIndicator: @Composable () -> Unit

    when (syncState) {
        SyncState.Refreshing -> {
            statusMessage = strings.statusMessageLoading
            statusIndicator = {}
        }

        is SyncState.Conflict -> {
            statusMessage = strings.statusMessageDataDiffer
            statusIndicator = {}
        }

        is SyncState.TrackingChanges -> {
            val uploadAvailable = syncState.uploadAvailable.collectAsState().value
            when {
                uploadAvailable -> {
                    statusMessage = strings.statusMessageLocalNewer
                    statusIndicator = {
                        IndicatorCircle(
                            MaterialTheme.extraColorScheme.due,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                else -> {
                    statusMessage = strings.statusMessageUpToDate
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
            statusMessage = strings.statusMessageError
            statusIndicator = { IndicatorCircle(MaterialTheme.colorScheme.error) }
        }

        SyncState.Uploading -> {
            statusMessage = strings.statusMessageUploading
            statusIndicator = {}
        }

        SyncState.Downloading -> {
            statusMessage = strings.statusMessageDownloading
            statusIndicator = {}
        }

        SyncState.Canceled -> {
            statusMessage = strings.statusMessageCanceled
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
        headlineContent = { Text(strings.statusTitle) },
        supportingContent = { Text(statusMessage) },
        trailingContent = statusIndicator
    )
}


@Composable
private fun SyncErrorListItem(issue: ApiRequestIssue) {

    val title: String
    val message: String
    val strings = resolveString { sync }

    when (issue) {
        ApiRequestIssue.NoConnection -> {
            title = strings.errorNoConnectionTitle
            message = strings.errorNoConnectionMessage
        }

        ApiRequestIssue.NoSubscription -> {
            title = strings.errorNoSubscriptionTitle
            message = strings.errorNoSubscriptionMessage
        }

        ApiRequestIssue.NotAuthenticated -> {
            title = strings.errorSessionExpiredTitle
            message = strings.errorSessionExpiredMessage
        }

        is ApiRequestIssue.Other -> {
            title = strings.errorOtherTitle
            message = issue.throwable.message ?: strings.errorOtherMessageFallback
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
private fun LocalSyncDataListItem(
    localData: PreferencesSyncDataInfo
) {

    val strings = resolveString { sync }

    ListItem(
        modifier = Modifier.clip(MaterialTheme.shapes.medium),
        headlineContent = { Text(strings.localDataTitle) },
        supportingContent = {

            Column {

                Text(
                    text = strings.localDataIdTemplate.format(localData.dataId),
                    overflow = TextOverflow.Ellipsis
                )

                val formattedTimestamp = localData.dataTimestamp
                    ?.let { Instant.fromEpochMilliseconds(it) }
                    ?.toLocalDateTime()
                    ?.format(CommonDateTimeFormat)
                    ?: "-"

                Text(
                    text = strings.localDataTimestampTemplate.format(formattedTimestamp)
                )

            }
        }
    )
}
