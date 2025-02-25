package ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.IndicatorCircle

private val SponsorIcon: ImageVector = Icons.Outlined.Handshake

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenUI(
    availableTabs: List<HomeScreenTab>,
    selectedTabState: State<HomeScreenTab>,
    syncIconState: SyncIconState,
    onTabSelected: (HomeScreenTab) -> Unit,
    onSyncButtonClick: () -> Unit,
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

                Row {
                    Text(
                        text = resolveString { appName },
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.align(Alignment.Bottom)
                    )

                    SyncButton(
                        state = syncIconState,
                        onClick = onSyncButtonClick
                    )
                }

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

            Surface(Modifier.weight(1f)) { screenTabContent.invoke() }

        }

    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Crossfade(
                            targetState = selectedTabState.value,
                            modifier = Modifier.width(IntrinsicSize.Max)
                        ) {
                            Text(
                                text = resolveString(it.titleResolver),
                                modifier = Modifier.fillMaxWidth().wrapContentWidth()
                            )
                        }
                    },
                    actions = {
                        SyncButton(
                            state = syncIconState,
                            onClick = onSyncButtonClick
                        )
                        IconButton(onClick = onSponsorButtonClick) {
                            Icon(SponsorIcon, null)
                        }
                    }
                )
            },
            bottomBar = {

                Column(Modifier.shadow(10.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                            .wrapContentWidth(align = Alignment.CenterHorizontally)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableTabs.forEach { tab ->
                            VerticalTabButton(
                                tab = tab,
                                selected = selectedTabState.value == tab,
                                onClick = { onTabSelected(tab) }
                            )
                        }
                    }
                }

            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .consumeWindowInsets(it)
            ) {
                screenTabContent.invoke()
            }

        }

    }

}

@Composable
private fun SyncButton(
    state: SyncIconState,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
    ) {

        IconButton(
            onClick = onClick
        ) {

            val rotation = rememberSyncIconRotation(state.loading)

            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = null,
                modifier = Modifier.graphicsLayer { rotationZ = rotation.value }
            )

        }

        AnimatedContent(
            targetState = state.indicator.value,
            transitionSpec = { scaleIn() togetherWith scaleOut() },
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            when (it) {
                SyncIconIndicator.Disabled,
                SyncIconIndicator.Conflict -> {
                    Box(Modifier.size(10.dp))
                }

                SyncIconIndicator.PendingUpload -> {
                    IndicatorCircle(MaterialTheme.extraColorScheme.due)
                }

                SyncIconIndicator.UpToDate -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = MaterialTheme.extraColorScheme.success,
                                shape = MaterialTheme.shapes.extraSmall
                            ),
                        tint = MaterialTheme.colorScheme.surface
                    )
                }

                SyncIconIndicator.Canceled -> {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }

                SyncIconIndicator.Error -> {
                    IndicatorCircle(MaterialTheme.colorScheme.primary)
                }
            }

        }

    }

}

@Composable
private fun rememberSyncIconRotation(animate: State<Boolean>): Animatable<Float, AnimationVector1D> {
    val rotation = remember { Animatable(360f) }

    LaunchedEffect(Unit) {
        var shouldLoop = false

        val animateLoop = suspend {
            while (shouldLoop) {
                rotation.snapTo(360f)
                rotation.animateTo(0f, tween(2000, 200))
            }
        }

        var animateLoopJob: Job? = null

        snapshotFlow { animate.value }.collect { shouldAnimate ->
            shouldLoop = shouldAnimate
            if (shouldAnimate) {
                val currentJob = animateLoopJob
                if (currentJob == null || currentJob.isCompleted)
                    animateLoopJob = launch { animateLoop() }
            }
        }
    }

    return rotation
}

@Composable
private fun RowScope.VerticalTabButton(
    tab: HomeScreenTab,
    selected: Boolean,
    onClick: () -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor = animateColorAsState(
        if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .wrapContentWidth()
            .size(48.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor.value)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
    ) {
        tab.iconContent()
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
