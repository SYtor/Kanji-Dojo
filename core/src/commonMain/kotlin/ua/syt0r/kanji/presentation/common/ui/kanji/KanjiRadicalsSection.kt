package ua.syt0r.kanji.presentation.common.ui.kanji

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

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
    val coloredStrokes = strokes.mapIndexed { index, path ->
        ColoredStroke(
            path = path,
            color = when (index in radicalDetails.strokeIndicies) {
                true -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        )
    }

    ListItem(
        leadingContent = {
            ClickableLetter(
                letter = radicalDetails.value,
                onClick = onRadicalClick
            )
        },
        headlineContent = {
            radicalDetails.meanings.joinToString().let { Text(it) }
        },
        trailingContent = {
            RadicalKanji(
                strokes = coloredStrokes,
                modifier = Modifier.size(40.dp)
            )
        }
    )
}