@file:OptIn(ExperimentalMaterial3Api::class)

package ua.syt0r.kanji.presentation.common

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import kotlin.math.roundToInt


interface CollapsibleContainerState {

    val scrollBehavior: TopAppBarScrollBehavior

    val nestedScrollConnection: NestedScrollConnection
        get() = scrollBehavior.nestedScrollConnection

    suspend fun expand()

}

@Composable
fun rememberCollapsibleContainerState(): CollapsibleContainerState {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    return remember { DefaultCollapsibleContainerState(scrollBehavior) }
}

@Composable
fun CollapsibleContainer(
    state: CollapsibleContainerState = rememberCollapsibleContainerState(),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    val scrollBehavior = state.scrollBehavior

    var heightOffsetLimit by remember {
        mutableFloatStateOf(0f)
    }
    LaunchedEffect(heightOffsetLimit) {
        if (scrollBehavior.state.heightOffsetLimit != heightOffsetLimit) {
            scrollBehavior.state.heightOffsetLimit = heightOffsetLimit
        }
    }

    val appBarDragModifier = if (!scrollBehavior.isPinned) {
        Modifier.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta -> scrollBehavior.state.heightOffset += delta },
            onDragStopped = { velocity ->
                settleAppBar(
                    scrollBehavior.state,
                    velocity,
                    scrollBehavior.flingAnimationSpec,
                    scrollBehavior.snapAnimationSpec
                )
            }
        )
    } else {
        Modifier
    }

    Layout(
        content = content,
        modifier = modifier.then(appBarDragModifier).clipToBounds(),
        measurePolicy = { measurables, constraints ->
            val placeable = measurables.firstOrNull()
                ?.measure(constraints.copy(minWidth = 0))
                ?: return@Layout layout(constraints.minWidth, constraints.minHeight) {}

            heightOffsetLimit = placeable.height.toFloat() * -1
            val scrollOffset = scrollBehavior.state.heightOffset
            val height = placeable.height.toFloat() + scrollOffset
            val layoutHeight = height.roundToInt()
            layout(constraints.maxWidth, layoutHeight) {
                placeable.place(0, scrollOffset.toInt())
            }
        }
    )
}

private class DefaultCollapsibleContainerState(
    override val scrollBehavior: TopAppBarScrollBehavior
) : CollapsibleContainerState {

    override suspend fun expand() {
        AnimationState(scrollBehavior.state.heightOffset)
            .animateTo(0f) { scrollBehavior.state.heightOffset = value }
    }

}

/**
 * Settles the app bar by flinging, in case the given velocity is greater than zero, and snapping
 * after the fling settles.
 */
@OptIn(ExperimentalMaterial3Api::class)
private suspend fun settleAppBar(
    state: TopAppBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?
): Velocity {
    // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
    // and just return Zero Velocity.
    // Note that we don't check for 0f due to float precision with the collapsedFraction
    // calculation.
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapse the app bar.
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = velocity,
        )
            .animateDecay(flingAnimationSpec) {
                val delta = value - lastValue
                val initialHeightOffset = state.heightOffset
                state.heightOffset = initialHeightOffset + delta
                val consumed = abs(initialHeightOffset - state.heightOffset)
                lastValue = value
                remainingVelocity = this.velocity
                // avoid rounding errors and stop if anything is unconsumed
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
    }
    // Snap if animation specs were provided.
    if (snapAnimationSpec != null) {
        if (state.heightOffset < 0 &&
            state.heightOffset > state.heightOffsetLimit
        ) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.heightOffsetLimit
                },
                animationSpec = snapAnimationSpec
            ) { state.heightOffset = value }
        }
    }

    return Velocity(0f, remainingVelocity)
}
