package ua.syt0r.kanji.presentation.common.ui

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Popup

@Composable
actual fun MultiplatformPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    preferredPopupLocation: PreferredPopupLocation,
    content: @Composable ColumnScope.() -> Unit
) {
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded

    if (expandedStates.currentState || expandedStates.targetState) {

        Popup(
            onDismissRequest = onDismissRequest,
            focusable = true,
            popupPositionProvider = remember { CustomPopupPositionProvider(preferredPopupLocation) }
        ) {

            val transition = updateTransition(expandedStates, "DropDownMenu")
            val scale by transition.animateFloat(label = "DropDownMenuScale") {
                if (it) 1f else 0f
            }
            val alpha by transition.animateFloat(label = "DropDownMenuScaleAlpha") {
                if (it) 1f else 0f
            }

            Card(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .width(IntrinsicSize.Max)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                content()
            }

        }
    }
}