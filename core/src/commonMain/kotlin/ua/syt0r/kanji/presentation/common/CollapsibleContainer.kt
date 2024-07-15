package ua.syt0r.kanji.presentation.common

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt


interface CollapsibleContainerState {

    val heightOffset: MutableState<Int>
    val containerHeight: MutableState<Int>

    val nestedScrollConnection: NestedScrollConnection
    suspend fun expand()

    fun getScrollDelta(delta: Float): Float
    fun dispatchScrollDeltaY(delta: Float)


}

@Composable
fun rememberCollapsibleContainerState(): CollapsibleContainerState {
    return remember { DefaultCollapsibleContainerState() }
}

@Composable
fun CollapsibleContainer(
    state: CollapsibleContainerState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    val appBarDragModifier = Modifier
        .scrollable(
            state = rememberScrollableState { delta ->
                val delta = state.getScrollDelta(delta)
                state.dispatchScrollDeltaY(delta)
                delta
            },
            orientation = Orientation.Vertical
        )

    Layout(
        content = content,
        modifier = modifier.then(appBarDragModifier)
            .clipToBounds(),
        measurePolicy = { measurables, constraints ->
            val placeable = measurables.firstOrNull()
                ?.measure(constraints.copy(minWidth = 0, maxHeight = Constraints.Infinity))
                ?: return@Layout layout(constraints.minWidth, constraints.minHeight) {}

            state.containerHeight.value = placeable.height
            val translationY = state.heightOffset.value.toFloat().roundToInt()
                .coerceIn(-placeable.height, 0)

            layout(constraints.maxWidth, placeable.height + translationY) {
                placeable.place(0, translationY)
            }
        }
    )
}

private class DefaultCollapsibleContainerState : CollapsibleContainerState {

    override val containerHeight: MutableState<Int> = mutableStateOf(0)
    override var heightOffset: MutableState<Int> = mutableStateOf(0)

    override var nestedScrollConnection = object : NestedScrollConnection {

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = getScrollDelta(available.y)
            dispatchScrollDeltaY(delta)
            return Offset(0f, delta)
        }

    }

    override suspend fun expand() {
        AnimationState(heightOffset.value.toFloat())
            .animateTo(0f) { heightOffset.value = value.roundToInt() }
    }

    override fun getScrollDelta(delta: Float): Float {
        val currentOffset = heightOffset.value.toFloat()
        val newOffset = (currentOffset + delta)
            .coerceIn(-containerHeight.value.toFloat(), 0f)
        return newOffset - currentOffset
    }

    override fun dispatchScrollDeltaY(delta: Float) {
        heightOffset.value = (heightOffset.value + delta).roundToInt()
    }

}
