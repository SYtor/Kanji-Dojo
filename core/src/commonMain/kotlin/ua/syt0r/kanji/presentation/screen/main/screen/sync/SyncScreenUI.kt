package ua.syt0r.kanji.presentation.screen.main.screen.sync

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import ua.syt0r.kanji.presentation.common.theme.snapToBiggerContainerCrossfadeTransitionSpec
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.sync.SyncScreenContract.ScreenState

@Composable
fun SyncScreenUI(
    onUpClick: () -> Unit,
    state: State<ScreenState>
) {

    ScreenContainer(
        state = state,
        onUpClick = onUpClick,
        loading = { FancyLoading(Modifier.fillMaxSize().wrapContentSize()) },
        loaded = { screenState ->

            Column {
                ListItem(
                    headlineContent = { Text("Auto sync") },
                    supportingContent = { Text("Update remote data when closing the application") },
                    trailingContent = {
                        Switch(
                            checked = screenState.autoSync.value,
                            onCheckedChange = { screenState.autoSync.value = it }
                        )
                    }
                )
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
    loaded: @Composable (screenState: ScreenState.Loaded) -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
                is ScreenState.Loaded -> loaded(screenState)
            }

        }

    }

}