package ua.syt0r.kanji.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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

    val listCoordinatesState = remember { mutableStateOf<LayoutCoordinates?>(null) }
    val overlayCoordinatesState = remember { mutableStateOf<LayoutCoordinates?>(null) }
    val extraOverlayBottomSpacingData = remember {
        ExtraOverlayBottomSpacingData(listCoordinatesState, overlayCoordinatesState)
    }

    Box(modifier) {
        Column(
            modifier = Modifier.fillMaxSize()
                .onGloballyPositioned { listCoordinatesState.value = it }
                .verticalScroll(rememberScrollState())
        ) {
            columnContent()
            extraOverlayBottomSpacingData.ExtraSpacer()
        }
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .onGloballyPositioned { overlayCoordinatesState.value = it }
        ) { bottomOverlayContent() }
    }

}


data class ExtraOverlayBottomSpacingData(
    val listCoordinatesState: State<LayoutCoordinates?>,
    val overlayCoordinatesState: State<LayoutCoordinates?>
) {

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
