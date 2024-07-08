package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.minus
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.core.PointF
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import kotlin.math.max


@Composable
fun LastWeekStudy(
    learnData: List<Int> = listOf(0, 12, 6, 6, 0, 6, 6),
    reviewData: List<Int> = listOf(18, 3, 0, 3, 12, 0, 5),
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        GraphCanvas(learnData, reviewData, Modifier.fillMaxWidth().weight(1f))
        Row(Modifier.fillMaxWidth().wrapContentSize()) {
            IndicatorTextRow(MaterialTheme.extraColorScheme.new, "Learn")
            IndicatorTextRow(MaterialTheme.extraColorScheme.due, "Review")
        }
    }
}

@Composable
private fun GraphCanvas(
    learnData: List<Int>,
    reviewData: List<Int>,
    modifier: Modifier = Modifier
) {

    val textColor = MaterialTheme.colorScheme.onSurface
    val textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light)
    val textMeasure = rememberTextMeasurer()
    val newColor = MaterialTheme.extraColorScheme.new
    val dueColor = MaterialTheme.extraColorScheme.due

    Canvas(
        modifier = modifier
    ) {

        val maxValue = max(learnData.max(), reviewData.max())

        val xAxisLabel = textMeasure.measure("Days ago", textStyle)
        val xAxisStartLabel = textMeasure.measure("7", textStyle)
        val xAxisEndLabel = textMeasure.measure("0", textStyle)
        val yAxisStartLabel = textMeasure.measure("1", textStyle)
        val yAxisEndLabel = textMeasure.measure(maxValue.toString(), textStyle)
        val yAxisLabel = textMeasure.measure("Reviews", textStyle)

        val axisLocation = Offset(
            x = yAxisLabel.size.height.toFloat(),
            y = size.height - xAxisLabel.size.height
        )

        var a: SeriesPlacementData? = null

        clipRect(left = axisLocation.x, top = size.height - axisLocation.y) {
            a = drawWeekSeries(learnData, axisLocation, maxValue, newColor)
            drawWeekSeries(reviewData, axisLocation, maxValue, dueColor)
        }

        val yLabelOffset = Offset(
            x = yAxisLabel.size.height / 2f,
            y = (axisLocation.y + a!!.yRangeLocation) / 2
        )
        rotate(270f, yLabelOffset) {
            drawText(
                textLayoutResult = yAxisLabel,
                topLeft = yLabelOffset.minus(yAxisLabel.size.center),
                color = textColor
            )
        }

        val yTopLabelOffset = Offset(x = yLabelOffset.x, y = a!!.yRangeLocation)
        rotate(270f, yTopLabelOffset) {
            drawText(
                textLayoutResult = yAxisEndLabel,
                topLeft = yTopLabelOffset.minus(yAxisEndLabel.size.center),
                color = textColor
            )
        }

        drawText(
            textLayoutResult = xAxisLabel,
            topLeft = Offset(
                size.width / 2 - xAxisLabel.size.width / 2,
                size.height - xAxisLabel.size.height
            ),
            color = textColor
        )

    }
}

fun DrawScope.drawWeekSeries(
    data: List<Int>,
    axisStart: Offset,
    maxValue: Int,
    color: Color,
): SeriesPlacementData {

    val strokeWidth = 4.dp.toPx()

    fun getX(day: Int): Float {
        return axisStart.x + (size.width) / (data.size - 1) * day
    }

    val verticalRange = maxValue * 1.2f

    fun getY(reviews: Int): Float {
        return axisStart.y - axisStart.y / verticalRange * reviews - strokeWidth / 2
    }

    val startPoint = PointF(getX(0), getY(data.first()))

    val fillPath = Path()
    val linePath = Path()

    val points = data.mapIndexed { index, i -> PointF(getX(index), getY(i)) }
    val (connectionPoints1, connectionPoints2) = calculateConnectionPointsForBezierCurve(points)

    listOf(fillPath, linePath).forEach { path ->
        path.apply {
            moveTo(startPoint.x, startPoint.y)
            for (i in 1 until points.size) {
                path.cubicTo(
                    connectionPoints1[i - 1].x,
                    connectionPoints1[i - 1].y,
                    connectionPoints2[i - 1].x,
                    connectionPoints2[i - 1].y,
                    points[i].x,
                    points[i].y
                )
            }
        }
    }

    fillPath.apply {
        lineTo(getX(6), getY(0))
        lineTo(getX(0), getY(0))
        lineTo(getX(0), startPoint.y)
    }

    drawPath(
        fillPath,
        Brush.verticalGradient(colors = listOf(color, Color.Transparent))
    )

    drawPath(linePath, color, style = Stroke(strokeWidth, cap = StrokeCap.Round))

    return SeriesPlacementData(
        yRangeLocation = getY(maxValue)
    )
}

class SeriesPlacementData(
    val yRangeLocation: Float
)


private fun calculateConnectionPointsForBezierCurve(
    points: List<PointF>
): Pair<List<PointF>, List<PointF>> {
    val connectionPoints1 = mutableListOf<PointF>()
    val connectionPoints2 = mutableListOf<PointF>()
    for (i in 1 until points.size) {
        connectionPoints1.add(PointF((points[i].x + points[i - 1].x) / 2, points[i - 1].y))
        connectionPoints2.add(PointF((points[i].x + points[i - 1].x) / 2, points[i].y))
    }
    return connectionPoints1 to connectionPoints2
}

@Composable
private fun IndicatorTextRow(
    color: Color,
    text: String
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = 10.dp)
    ) {

        Box(
            modifier = Modifier
                .alignBy { it.measuredHeight }
                .size(width = 20.dp, height = 10.dp)
                .clip(CircleShape)
                .background(color)
        )

        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                    )
                ) { append(text) }
            },
            modifier = Modifier.alignByBaseline()
        )
    }

}
