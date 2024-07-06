package ua.syt0r.kanji.presentation.common.ui.kanji

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import kotlin.math.max


@Composable
fun KanjiReadingsContainer(
    on: List<String>,
    kun: List<String>,
    modifier: Modifier = Modifier
) {

    Layout(
        content = {
            on.takeIf { it.isNotEmpty() }?.also {
                ReadingsLabel(resolveString { onyomi })
                ReadingsRow(it)
            }

            kun.takeIf { it.isNotEmpty() }?.also {
                ReadingsLabel(resolveString { kunyomi })
                ReadingsRow(it)
            }
        },
        modifier = modifier
    ) { measurables, constraints ->

        val horizontalSpacingPx = 8.dp.roundToPx()
        val verticalSpacingPx = 4.dp.roundToPx()

        val labelPlaceables = listOfNotNull(
            measurables.getOrNull(0),
            measurables.getOrNull(2)
        ).map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        val maxLabelWidth = labelPlaceables.maxOf { it.width }
        val readingsWidth = constraints.maxWidth - maxLabelWidth - horizontalSpacingPx

        val readingRowsPlaceables = listOfNotNull(
            measurables.getOrNull(1),
            measurables.getOrNull(3)
        ).map {
            it.measure(constraints.copy(maxWidth = readingsWidth, minWidth = 0, minHeight = 0))
        }

        layout(
            width = constraints.maxWidth,
            height = max(
                labelPlaceables.sumOf { it.height } + verticalSpacingPx,
                readingRowsPlaceables.sumOf { it.height } + verticalSpacingPx
            )
        ) {

            var y = 0
            labelPlaceables.zip(readingRowsPlaceables).forEachIndexed { index, (label, row) ->
                val labelBaseLine = label[FirstBaseline]
                val rowBaseLine = row[FirstBaseline]
                val labelShift = rowBaseLine - labelBaseLine

                label.place(0, y + labelShift)
                row.place(maxLabelWidth + horizontalSpacingPx, y)
                y += max(label.height, row.height) + verticalSpacingPx
            }

        }
    }

}

@Composable
private fun ReadingsLabel(title: String) {
    Text(
        text = title,
        modifier = Modifier.width(IntrinsicSize.Max)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadingsRow(readings: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        readings.forEach {

            Text(
                text = it,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                maxLines = 1
            )

        }

    }
}
