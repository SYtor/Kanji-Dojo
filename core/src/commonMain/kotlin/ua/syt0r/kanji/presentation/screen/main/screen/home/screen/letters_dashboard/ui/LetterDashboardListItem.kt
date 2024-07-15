package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.ui

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
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardItem


@Composable
fun LetterDashboardListItem(
    item: LettersDashboardItem,
    dailyGoalEnabled: Boolean,
    onItemClick: () -> Unit,
    quickPractice: (MainDestination.Practice) -> Unit
) {

    var expanded by rememberSaveable(item.deckId) { mutableStateOf(false) }

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

            Column(
                modifier = Modifier.weight(1f)
                    .padding(start = 16.dp)
                    .padding(vertical = 10.dp),
            ) {

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = resolveString {
                        lettersDashboard.itemTimeMessage(item.timeSinceLastReview)
                    },
                    style = MaterialTheme.typography.bodySmall,
                )

            }

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                DeckPendingReviewsCountIndicator(
                    icon = Icons.Default.Draw,
                    dailyGoalEnabled = dailyGoalEnabled,
                    study = item.writingProgress.quickLearn.size,
                    review = item.writingProgress.quickReview.size
                )

                DeckPendingReviewsCountIndicator(
                    icon = Icons.Default.LocalLibrary,
                    dailyGoalEnabled = dailyGoalEnabled,
                    study = item.readingProgress.quickLearn.size,
                    review = item.readingProgress.quickReview.size
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(onClick = onItemClick)
                    .aspectRatio(1f)
                    .wrapContentSize()
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            }

        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            ListItemDetails(item, quickPractice)
        }

    }

}

@Composable
private fun DeckPendingReviewsCountIndicator(
    icon: ImageVector,
    dailyGoalEnabled: Boolean,
    study: Int,
    review: Int
) {
    val showStudy = study > 0 && dailyGoalEnabled
    val showDue = review > 0
    if (!showStudy && !showDue) return

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        if (showStudy) {
            Box(
                modifier = Modifier.align(Alignment.CenterVertically).size(4.dp)
                    .background(MaterialTheme.extraColorScheme.new, CircleShape)
            )
        }
        if (showDue) {
            Box(
                modifier = Modifier.align(Alignment.CenterVertically).size(4.dp)
                    .background(MaterialTheme.extraColorScheme.due, CircleShape)
            )
        }
    }
}

@Composable
private fun ListItemDetails(
    data: LettersDashboardItem,
    quickPractice: (MainDestination.Practice) -> Unit
) {

    val strings = resolveString { lettersDashboard }

    val isReadingMode = rememberSaveable(data.deckId) { mutableStateOf(false) }
    val studyProgress by remember {
        derivedStateOf { if (isReadingMode.value) data.readingProgress else data.writingProgress }
    }

    val onQuickPracticeButtonClick: (characters: List<String>) -> Unit = lambda@{
        if (it.isEmpty()) return@lambda
        val destination = when (isReadingMode.value) {
            true -> MainDestination.Practice.Reading(data.deckId, it)
            false -> MainDestination.Practice.Writing(data.deckId, it)
        }
        quickPractice(destination)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {

        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Bottom
        ) {

            Column(
                modifier = Modifier.weight(1f).fillMaxSize()
            ) {

                PracticeTypeSwitch(isReadingMode = isReadingMode)

                IndicatorTextRow(
                    color = MaterialTheme.colorScheme.outline,
                    label = strings.itemTotal,
                    characters = studyProgress.all,
                    onClick = onQuickPracticeButtonClick
                )

                IndicatorTextRow(
                    color = MaterialTheme.extraColorScheme.success,
                    label = strings.itemDone,
                    characters = studyProgress.known,
                    onClick = onQuickPracticeButtonClick
                )

                IndicatorTextRow(
                    color = MaterialTheme.extraColorScheme.due,
                    label = strings.itemReview,
                    characters = studyProgress.review,
                    onClick = onQuickPracticeButtonClick
                )

                IndicatorTextRow(
                    color = MaterialTheme.extraColorScheme.new,
                    label = strings.itemNew,
                    characters = studyProgress.new,
                    onClick = onQuickPracticeButtonClick
                )

            }

            Box(
                modifier = Modifier.size(120.dp)
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
                        ) { append(strings.itemGraphProgressValue(studyProgress.completionPercentage)) }
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )

            }

        }

        Text(
            text = strings.itemQuickPracticeTitle,
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            QuickPracticeButton(
                enabled = studyProgress.quickLearn.isNotEmpty(),
                text = strings.itemQuickPracticeLearn(studyProgress.quickLearn.size),
                onClick = { onQuickPracticeButtonClick(studyProgress.quickLearn) }
            )

            QuickPracticeButton(
                enabled = studyProgress.quickReview.isNotEmpty(),
                text = strings.itemQuickPracticeReview(studyProgress.quickReview.size),
                onClick = { onQuickPracticeButtonClick(studyProgress.quickReview) }
            )

        }

    }

}

@Composable
private fun ColumnScope.IndicatorTextRow(
    color: Color,
    label: String,
    characters: List<String>,
    onClick: (List<String>) -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth(fraction = 0.8f)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = { onClick(characters) })
            .padding(horizontal = 10.dp)
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
                ) { append(" ${characters.size}") }
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
private fun ColumnScope.PracticeTypeSwitch(
    isReadingMode: MutableState<Boolean>
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.align(Alignment.Start)
    ) {

        Switch(
            checked = isReadingMode.value,
            onCheckedChange = { isReadingMode.value = !isReadingMode.value },
            thumbContent = {
                val icon = if (isReadingMode.value) Icons.Default.LocalLibrary
                else Icons.Default.Draw
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.outline,
                checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                checkedIconColor = MaterialTheme.colorScheme.surfaceVariant,
                checkedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedIconColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        )

        Text(
            text = resolveString {
                if (isReadingMode.value) lettersDashboard.itemReadingTitle else lettersDashboard.itemWritingTitle
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraLight
        )

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
        enabled = enabled
    ) {
        Text(text)
    }

}
