package ua.syt0r.kanji.presentation.common.ui.kanji

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

private const val ColorsInPalette = 10
private val StrokeColorPalette: List<Color> = (0 until ColorsInPalette)
    .asSequence()
    .map {
        val hue = 360f / ColorsInPalette * it
        Color.hsv(hue, 0.67f, 0.9f)
    }
    .toList()

fun getColoredKanjiStrokes(
    strokes: List<Path>,
    radicalToStrokeRangeList: List<Pair<String, IntRange>>
): List<ColoredStroke> {

    val strokeIndexToRadical: Map<Int, String?> = strokes.indices
        .associateWith { strokeIndex ->
            radicalToStrokeRangeList
                .filter { strokeIndex in it.second }
                .maxByOrNull { it.second.run { endInclusive - start } }
                ?.first
        }

    var colorIndex = 0
    return strokes.mapIndexed { index, path ->
        val radical = strokeIndexToRadical[index]
        val color = StrokeColorPalette[colorIndex % StrokeColorPalette.size]

        if (strokeIndexToRadical[index + 1].let { it != radical || it == null }) {
            colorIndex++
        }

        ColoredStroke(path, color)
    }
}

data class ColoredStroke(
    val path: Path,
    val color: Color
)

@Composable
fun RadicalKanji(
    strokes: List<ColoredStroke>,
    modifier: Modifier = Modifier
) {

    Box(modifier = modifier) {
        strokes.forEach { (path, color) ->
            Stroke(
                path = path,
                color = color,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

}

//@Preview
//@Composable
//private fun Preview(darkTheme: Boolean = false) {
//    AppTheme(darkTheme) {
//        Surface {
//            RadicalKanji(
//                strokes = PreviewKanji.strokes,
//                radicals = PreviewKanji.radicals,
//                modifier = Modifier
//                    .padding(60.dp)
//                    .size(80.dp)
//            )
//        }
//    }
//}
//
//@Preview
//@Composable
//private fun DarkPreview() {
//    Preview(darkTheme = true)
//}
