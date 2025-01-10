package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.RefreshableStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.TimePeriodStats
import kotlin.math.ceil


@Composable
fun StatsYearCalendar(
    today: LocalDate,
    stats: RefreshableStats<Int, TimePeriodStats>
) {

    val selectedYear = stats.selectedTimePeriodState.value
    val reviewDates = stats.statsState.value.dateToReviewsMap

    val yearStartDate = LocalDate(selectedYear, 1, 1)
    val yearEndDate = LocalDate(selectedYear + 1, 1, 1).minus(1, DateTimeUnit.DAY)

    val daysInYear = yearEndDate.dayOfYear
    val daysInFirstWeekLastYear = yearStartDate.dayOfWeek.isoDayNumber - 1
    val daysInLastWeekNextYear = 7 - yearEndDate.dayOfWeek.isoDayNumber
    val totalDays = daysInYear + daysInFirstWeekLastYear + daysInLastWeekNextYear

    val weeks = totalDays / 7

    val boxSize = 12.dp
    val margins = 2.dp

    val calendarWidth = boxSize * weeks + margins * (weeks - 1)

    val initialScrollDp = calendarWidth * (today.dayOfYear / totalDays.toFloat()) - 200.dp
    val initialScrollPx = with(LocalDensity.current) { initialScrollDp.roundToPx() }

    val defaultColor = MaterialTheme.colorScheme.surfaceVariant
    val practicedColor = MaterialTheme.colorScheme.primary
    val todayColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = Modifier.horizontalScroll(rememberScrollState(initialScrollPx))
            .size(
                width = calendarWidth,
                height = boxSize * 7 + margins * 6
            )
    ) {

        val size = Size(boxSize.toPx(), boxSize.toPx())
        var weekStart = yearStartDate

        do {
            val weekIndex = ceil(yearStartDate.daysUntil(weekStart) / 7f)
            val weekDays = (weekStart.dayOfWeek.isoDayNumber..7)
                .mapIndexed { index, i -> weekStart.plus(index, DateTimeUnit.DAY) }

            for (day in weekDays) {
                val dayIndex = day.dayOfWeek.isoDayNumber - 1
                val dayOffset = Offset(
                    x = boxSize.toPx() * weekIndex + margins.toPx() * weekIndex,
                    y = boxSize.toPx() * dayIndex + margins.toPx() * dayIndex
                )
                drawRoundRect(
                    color = if (reviewDates.contains(day)) practicedColor else defaultColor,
                    topLeft = dayOffset,
                    size = size,
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                if (day == today) {
                    val centerOffset = boxSize.toPx() / 2
                    drawCircle(
                        color = todayColor,
                        radius = size.minDimension / 4,
                        center = dayOffset.plus(Offset(centerOffset, centerOffset))
                    )
                }
            }

            weekStart = weekStart.plus(weekDays.size, DateTimeUnit.DAY)
        } while (weekStart.year == selectedYear)

    }

}