package ua.syt0r.kanji.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combineTransform
import kotlin.math.max

@Composable
fun AutopaddedScrollableColumn(
    modifier: Modifier,
    bottomOverlayContent: @Composable () -> Unit,
    columnContent: @Composable ColumnScope.() -> Unit
) {

    val extraListSpacerState = rememberExtraListSpacerState()

    Box(modifier) {
        Column(
            modifier = Modifier.fillMaxSize()
                .onGloballyPositioned { extraListSpacerState.updateList(it) }
                .verticalScroll(rememberScrollState())
        ) {
            columnContent()
            extraListSpacerState.ExtraSpacer()
        }
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .onGloballyPositioned { extraListSpacerState.updateOverlay(it) }
        ) { bottomOverlayContent() }
    }

}

@Composable
fun rememberExtraListSpacerState(): ExtraListSpacerState {
    return remember { ExtraListSpacerState() }
}


fun Modifier.trackList(state: ExtraListSpacerState): Modifier =
    onGloballyPositioned { state.updateList(it) }

fun Modifier.trackOverlay(state: ExtraListSpacerState): Modifier =
    onGloballyPositioned { state.updateOverlay(it) }

class ExtraListSpacerState {

    private val listCoordinatesState = mutableStateOf<LayoutCoordinates?>(null)
    private val overlayCoordinatesState = mutableStateOf<LayoutCoordinates?>(null)

    fun updateList(layoutCoordinates: LayoutCoordinates) {
        listCoordinatesState.value = layoutCoordinates
    }

    fun updateOverlay(layoutCoordinates: LayoutCoordinates) {
        overlayCoordinatesState.value = layoutCoordinates
    }

    @Composable
    fun ExtraSpacer(minimalSpacing: Dp = 16.dp) {
        val resultSpacing = rememberSaveable { mutableStateOf(minimalSpacing.value) }

        val density = LocalDensity.current
        LaunchedEffect(Unit) {
            snapshotFlow { listCoordinatesState.value }
                .combineTransform(
                    flow = snapshotFlow { overlayCoordinatesState.value },
                    transform = { a, b ->
                        if (a != null && b != null && a.isAttached && b.isAttached) {
                            emit(a to b)
                        }
                    }
                )
                .collect { (listCoords, overlayCoords) ->
                    val listBottomY = listCoords.positionInRoot().y + listCoords.size.height
                    val overlayTopY = overlayCoords.positionInRoot().y
                    val extraSpacing = with(density) { max(0f, listBottomY - overlayTopY).toDp() }
                    resultSpacing.value = minimalSpacing.value + extraSpacing.value
                }

        }
        Spacer(Modifier.height(resultSpacing.value.dp))
    }


}

fun ExtraListSpacerState.ExtraSpacer(scope: LazyGridScope, minimalSpacing: Dp = 16.dp) {
    scope.item(
        span = { GridItemSpan(maxLineSpan) }
    ) {
        ExtraSpacer(minimalSpacing)
    }
}

fun ExtraListSpacerState.ExtraSpacer(scope: LazyStaggeredGridScope, minimalSpacing: Dp = 16.dp) {
    scope.item(
        span = StaggeredGridItemSpan.FullLine
    ) {
        ExtraSpacer(minimalSpacing)
    }
}

fun ExtraListSpacerState.ExtraSpacer(scope: LazyListScope, minimalSpacing: Dp = 16.dp) {
    scope.item { ExtraSpacer(minimalSpacing) }
}