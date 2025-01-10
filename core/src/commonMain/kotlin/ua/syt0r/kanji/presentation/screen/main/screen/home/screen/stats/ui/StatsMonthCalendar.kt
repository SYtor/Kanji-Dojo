package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import ua.syt0r.kanji.presentation.common.copyCentered
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.IndicatorCircle
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.RefreshableStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.TimePeriodStats
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.YearMonth


private val DayLabels = listOf("月", "火", "水", "木", "金", "土", "日")

sealed interface MonthCalendarItem {

    val dayNumber: Int

    data class DayFromOtherMonth(override val dayNumber: Int) : MonthCalendarItem

    data class DayFromCurrentMonth(
        override val dayNumber: Int,
        val isPracticed: Boolean,
        val isToday: Boolean
    ) : MonthCalendarItem

}

@Composable
fun StatsMonthCalendar(
    today: LocalDate,
    monthStats: RefreshableStats<YearMonth, TimePeriodStats>
) {

    val yearMonth = monthStats.selectedTimePeriodState.value
    val currentStats = monthStats.statsState.value

    val firstDayOfMonth = yearMonth.monthStart
    val firstDay = firstDayOfMonth.minus(
        DatePeriod(days = firstDayOfMonth.dayOfWeek.isoDayNumber - 1)
    )

    val lastDayOfMonth = firstDayOfMonth.plus(1, DateTimeUnit.MONTH)
        .minus(1, DateTimeUnit.DAY)
    val lastDay = lastDayOfMonth.plus(
        value = 7 - lastDayOfMonth.dayOfWeek.isoDayNumber,
        unit = DateTimeUnit.DAY
    )

    val gridItems = mutableListOf<MonthCalendarItem>()

    var day = firstDay
    while (day <= lastDay) {
        if (day.month != yearMonth.month) {
            gridItems.add(MonthCalendarItem.DayFromOtherMonth(dayNumber = day.dayOfMonth))
        } else {
            gridItems.add(
                MonthCalendarItem.DayFromCurrentMonth(
                    dayNumber = day.dayOfMonth,
                    isPracticed = currentStats.dateToReviewsMap.containsKey(day),
                    isToday = day == today
                )
            )
        }
        day = day.plus(1, DateTimeUnit.DAY)
    }

    Column(
        modifier = Modifier.fillMaxWidth().wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {


        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DayLabels.forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f)
                        .wrapContentWidth()
                        .size(45.dp)
                        .wrapContentSize(),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.textDp
                )
            }
        }

        gridItems.chunked(7).forEach { weekDays ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                weekDays.forEach { MonthCalendarDay(it) }
            }
        }
    }

}

@Composable
private fun RowScope.MonthCalendarDay(item: MonthCalendarItem) {

    val bgColor: Color
    val textColor: Color
    val todayIndicatorColor: Color

    when (item) {
        is MonthCalendarItem.DayFromOtherMonth -> {
            bgColor = MaterialTheme.colorScheme.surface
            textColor = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            todayIndicatorColor = bgColor
        }

        is MonthCalendarItem.DayFromCurrentMonth -> {
            if (item.isPracticed) {
                bgColor = MaterialTheme.colorScheme.primary
                textColor = MaterialTheme.colorScheme.onPrimary
            } else {
                bgColor = MaterialTheme.colorScheme.surface
                textColor = MaterialTheme.colorScheme.onSurface
            }

            todayIndicatorColor = when {
                !item.isToday -> Color.Transparent
                else -> textColor
            }
        }
    }

    Layout(
        content = {
            Text(
                text = item.dayNumber.toString(),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium.copyCentered()
            )
            IndicatorCircle(
                color = todayIndicatorColor
            )
        },
        modifier = Modifier
            .weight(1f)
            .wrapContentWidth()
            .background(bgColor, MaterialTheme.shapes.medium)
            .padding(4.dp)
            .aspectRatio(1f)
            .wrapContentSize()
    ) { measurables, constraints ->
        val minConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val textPlaceable = measurables.first().measure(minConstraints)
        val indicatorPlaceable = measurables.last().measure(minConstraints)

        val height = textPlaceable.height + indicatorPlaceable.height * 2
        val width = height

        layout(width, height) {
            indicatorPlaceable.place(
                width - indicatorPlaceable.width,
                0
            )
            textPlaceable.place(
                width / 2 - textPlaceable.width / 2,
                height / 2 - textPlaceable.height / 2,
            )
        }
    }
}
