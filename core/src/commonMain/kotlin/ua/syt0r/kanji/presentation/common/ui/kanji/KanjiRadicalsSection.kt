package ua.syt0r.kanji.presentation.common.ui.kanji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString

data class KanjiRadicalsSectionData(
    val strokes: List<Path>,
    val radicals: List<KanjiRadicalDetails>
)

data class KanjiRadicalDetails(
    val value: String,
    val strokeIndicies: IntRange,
    val meanings: List<String>
)

@Composable
fun KanjiRadicalsSection(
    state: KanjiRadicalsSectionData,
    onRadicalClick: (String) -> Unit,
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = resolveString { info.radicalsSectionTitle(state.radicals.size) },
            style = MaterialTheme.typography.titleLarge
        )

        state.radicals.forEach { details ->
            RadicalDetailsRow(
                strokes = state.strokes,
                radicalDetails = details,
                onRadicalClick = onRadicalClick
            )
        }

    }

}

@Composable
private fun RadicalDetailsRow(
    strokes: List<Path>,
    radicalDetails: KanjiRadicalDetails,
    onRadicalClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        val coloredStrokes = strokes.mapIndexed { index, path ->
            ColoredStroke(
                path = path,
                color = when (index in radicalDetails.strokeIndicies) {
                    true -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface.copy(0.6f)
                }
            )
        }

        Row(
            modifier = Modifier.height(IntrinsicSize.Max).width(IntrinsicSize.Min)
        ) {
            Text(
                text = radicalDetails.value,
                fontSize = 32.sp,
                modifier = Modifier.clip(MaterialTheme.shapes.small)
                    .fillMaxHeight()
                    .aspectRatio(1f, true)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onRadicalClick(radicalDetails.value) }
                    .padding(8.dp)
                    .wrapContentSize(unbounded = true)
            )

            RadicalKanji(
                strokes = coloredStrokes,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f, true)
            )
        }

        radicalDetails.meanings.joinToString().let { Text(it) }

    }

}