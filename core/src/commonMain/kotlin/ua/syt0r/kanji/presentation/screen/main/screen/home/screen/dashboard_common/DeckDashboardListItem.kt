package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import kotlin.time.Duration


@Composable
fun DeckDashboardListItemContainer(
    itemKey: Any,
    header: @Composable RowScope.() -> Unit,
    details: @Composable () -> Unit
) {

    var expanded by rememberSaveable(itemKey) { mutableStateOf(false) }

    Column(
        modifier = Modifier.clip(MaterialTheme.shapes.large)
            .padding(horizontal = 20.dp)
    ) {

        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = { expanded = !expanded })
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            header()
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            details()
        }

    }

}

@Composable
fun RowScope.DeckDashboardListItemHeader(
    title: String,
    elapsedSinceLastReview: Duration?,
    onDetailsClick: () -> Unit,
    extraIndicatorContent: @Composable RowScope.() -> Unit
) {

    Column(
        modifier = Modifier.weight(1f)
            .padding(start = 16.dp)
            .padding(vertical = 10.dp),
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = resolveString {
                lettersDashboard.itemTimeMessage(elapsedSinceLastReview)
            },
            style = MaterialTheme.typography.bodySmall,
        )

    }

    extraIndicatorContent()

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(onClick = onDetailsClick)
            .aspectRatio(1f)
            .wrapContentSize()
    ) {
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
    }

}

@Composable
fun <T> DeckDashboardListItemDetails(
    studyProgress: DeckStudyProgress<T>,
    indicatorColumnTopContent: @Composable ColumnScope.() -> Unit,
    indicatorsRowContentAlignment: Alignment.Vertical,
    navigateToPractice: (List<T>) -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {

        val strings = resolveString { lettersDashboard }

        Row(
            modifier = Modifier.height(IntrinsicSize.Min).padding(end = 6.dp),
            verticalAlignment = indicatorsRowContentAlignment
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                indicatorColumnTopContent()

                IndicatorTextRow(
                    color = MaterialTheme.colorScheme.outline,
                    label = strings.itemTotal,
                    items = studyProgress.all,
                    onClick = navigateToPractice
                )

                IndicatorTextRow(
                    color = MaterialTheme.extraColorScheme.success,
                    label = strings.itemDone,
                    items = studyProgress.known,
                    onClick = navigateToPractice
                )

                IndicatorTextRow(
                    color = MaterialTheme.extraColorScheme.due,
                    label = strings.itemReview,
                    items = studyProgress.review,
                    onClick = navigateToPractice
                )

                IndicatorTextRow(
                    color = MaterialTheme.extraColorScheme.new,
                    label = strings.itemNew,
                    items = studyProgress.new,
                    onClick = navigateToPractice
                )

            }

            Box(
                modifier = Modifier.padding(vertical = 8.dp)
                    .size(120.dp)
            ) {

                PieIndicator(
                    max = studyProgress.all.size.toFloat(),
                    known = animateFloatAsState(targetValue = studyProgress.known.size.toFloat()),
                    review = animateFloatAsState(targetValue = studyProgress.review.size.toFloat()),
                    new = animateFloatAsState(targetValue = studyProgress.new.size.toFloat()),
                    modifier = Modifier.fillMaxSize()
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Light,
                                fontSize = 14.textDp,
                            )
                        ) { append(strings.itemGraphProgressTitle) }
                        append("\n")
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 22.textDp
                            )
                        ) {
                            append(strings.itemGraphProgressValue(studyProgress.completionPercentage()))
                        }
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )

            }

        }

        Text(
            text = strings.itemQuickPracticeTitle,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            QuickPracticeButton(
                enabled = studyProgress.quickLearn.isNotEmpty(),
                text = strings.itemQuickPracticeLearn(studyProgress.quickLearn.size),
                onClick = { navigateToPractice(studyProgress.quickLearn) }
            )

            QuickPracticeButton(
                enabled = studyProgress.quickReview.isNotEmpty(),
                text = strings.itemQuickPracticeReview(studyProgress.quickReview.size),
                onClick = { navigateToPractice(studyProgress.quickReview) }
            )

        }

    }

}

@Composable
private fun <T> ColumnScope.IndicatorTextRow(
    color: Color,
    label: String,
    items: List<T>,
    onClick: (List<T>) -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth(fraction = 0.8f)
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = items.isNotEmpty(), onClick = { onClick(items) })
            .padding(start = 10.dp, end = 4.dp)
    ) {

        Box(
            modifier = Modifier
                .size(10.dp)
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
                ) { append(label) }
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 22.sp
                    )
                ) { append(" ${items.size}") }
            },
            textAlign = TextAlign.Center
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.End).size(20.dp)
        )

    }

}

@Composable
private fun PieIndicator(
    max: Float,
    known: State<Float>,
    review: State<Float>,
    new: State<Float>,
    modifier: Modifier = Modifier,
) {

    val knownColor = MaterialTheme.extraColorScheme.success
    val newColor = MaterialTheme.extraColorScheme.new
    val dueColor = MaterialTheme.extraColorScheme.due

    Canvas(
        modifier = modifier
    ) {

        val strokeWidth = 10.dp.toPx()
        val strokeStyle = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        )
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val arcOffset = Offset(strokeWidth, strokeWidth).div(2f)

        if (max == 0f) {
            drawArc(
                size = arcSize,
                topLeft = arcOffset,
                color = knownColor,
                startAngle = 270f,
                sweepAngle = 360f,
                useCenter = false,
                style = strokeStyle
            )
            return@Canvas
        }

        val strokeParts = listOf(
            knownColor to known.value,
            dueColor to review.value,
            newColor to new.value,
        )

        var accumulatedAngle = 0f
        strokeParts.forEach { (color, value) ->
            drawArc(
                size = arcSize,
                topLeft = arcOffset,
                color = color,
                startAngle = 270f + accumulatedAngle / max * 360,
                sweepAngle = value / max * 360,
                useCenter = false,
                style = strokeStyle
            )
            accumulatedAngle += value
        }

    }
}

@Composable
private fun RowScope.QuickPracticeButton(
    enabled: Boolean,
    text: String,
    onClick: () -> Unit
) {

    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.medium,
        enabled = enabled
    ) {
        Text(text)
    }

}
