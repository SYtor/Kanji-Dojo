package ua.syt0r.kanji.presentation.common.ui.kanji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.copyCentered

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
fun KanjiRadicalUI(
    strokes: List<Path>,
    radicalDetails: KanjiRadicalDetails,
    onRadicalClick: (String) -> Unit,
) {
    ListItem(
        leadingContent = {
            Text(
                text = radicalDetails.value,
                fontSize = 32.sp,
                modifier = Modifier.clip(MaterialTheme.shapes.small)
                    .width(IntrinsicSize.Min)
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onRadicalClick(radicalDetails.value) }
                    .padding(8.dp)
                    .wrapContentSize(),
                style = MaterialTheme.typography.bodyLarge.copyCentered()
            )
        },
        headlineContent = {
            radicalDetails.meanings.joinToString().let { Text(it) }
        },
        trailingContent = {
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

                RadicalKanji(
                    strokes = coloredStrokes,
                    modifier = Modifier
                        .height(40.dp)
                        .aspectRatio(1f, true)
                )

            }
        },
        modifier = Modifier
    )

}