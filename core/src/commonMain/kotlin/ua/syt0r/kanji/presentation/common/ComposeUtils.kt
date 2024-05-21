package ua.syt0r.kanji.presentation.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.presentation.common.resources.string.resolveString

data class ItemPositionData(
    val density: Density,
    val layoutCoordinates: LayoutCoordinates,
    val heightFromScreenBottom: Dp
)

@Composable
fun Modifier.trackItemPosition(
    receiver: (ItemPositionData) -> Unit
): Modifier {
    val density = LocalDensity.current
    return onGloballyPositioned {
        if (!it.isAttached) return@onGloballyPositioned
        val screenHeightPx = it.findRootCoordinates().size.height
        val fabTopHeightPx = it.boundsInRoot().top
        val heightDp = (screenHeightPx - fabTopHeightPx) / density.density
        receiver(ItemPositionData(density, it, heightDp.dp))
    }
}

val json = Json { allowStructuredMapKeys = true }

inline fun <reified T> jsonSaver() = Saver<T, String>(
    save = { json.encodeToString(it) },
    restore = { json.decodeFromString(it) }
)

@Composable
fun CharacterClassification.Kana.resolveString(): String {
    val kana = this
    return resolveString {
        when (kana) {
            CharacterClassification.Kana.Hiragana -> hiragana
            CharacterClassification.Kana.Katakana -> katakana
        }
    }
}

expect val ExcludeNavigationGesturesModifier: Modifier

fun LazyListState.isNearListEnd(eligibleItemsFromEnd: Int): Boolean = layoutInfo.run {
    val lastVisibleItemIndex = visibleItemsInfo.lastOrNull()?.index ?: Int.MAX_VALUE
    val thresholdItemIndex = totalItemsCount - eligibleItemsFromEnd - 1
    lastVisibleItemIndex >= thresholdItemIndex
}


@Composable
fun getBottomLineShape(strokeThickness: Dp): Shape {
    val strokeThicknessPx = with(LocalDensity.current) { strokeThickness.toPx() }
    return GenericShape { size, _ ->
        moveTo(0f, size.height)
        lineTo(size.width, size.height)
        lineTo(size.width, size.height - strokeThicknessPx)
        lineTo(0f, size.height - strokeThicknessPx)
    }
}

val Int.textDp: TextUnit
    @Composable get() = with(LocalDensity.current) { this@textDp.dp.toSp() }
