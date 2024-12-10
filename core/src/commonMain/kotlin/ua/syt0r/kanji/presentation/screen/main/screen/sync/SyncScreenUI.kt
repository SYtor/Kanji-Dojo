package ua.syt0r.kanji.presentation.screen.main.screen.sync

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.ScrollableScreenContainer
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.snapToBiggerContainerCrossfadeTransitionSpec
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.sync.SyncScreenContract.ScreenState

@Composable
fun SyncScreenUI(
    onUpClick: () -> Unit,
    navigateToAccountScreen: () -> Unit,
    state: State<ScreenState>
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
                    isCompleted = screenState.isSignedIn,
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
                ListItem(
                    modifier = Modifier.clip(MaterialTheme.shapes.medium)
                        .clickable { screenState.autoSync.run { value = !value } },
                    headlineContent = { Text("Auto sync") },
                    supportingContent = { Text("Update remote data when closing the application") },
                    trailingContent = {
                        Switch(
                            checked = screenState.autoSync.value,
                            onCheckedChange = { screenState.autoSync.value = it }
                        )
                    }
                )

                ListItem(
                    modifier = Modifier.clip(MaterialTheme.shapes.medium)
                        .clickable { },
                    headlineContent = { Text("Sync now") },
                    trailingContent = { Icon(Icons.Default.Refresh, null) }
                )
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
