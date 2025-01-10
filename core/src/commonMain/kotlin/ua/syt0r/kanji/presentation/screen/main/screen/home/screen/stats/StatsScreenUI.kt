package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.AutoSizeText
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.StatsScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.ui.StatsMonthCalendar
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.stats.ui.StatsYearCalendar

@Composable
fun StatsScreenUI(
    state: State<ScreenState>
) {

    AnimatedContent(
        targetState = state.value,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = Modifier.fillMaxSize()
    ) {

        when (it) {
            ScreenState.Loading -> {
                FancyLoading(modifier = Modifier.fillMaxSize().wrapContentSize())
            }

            is ScreenState.Loaded -> {
                LoadedState(it)
            }
        }

    }


}

@Composable
private fun LoadedState(screenState: ScreenState.Loaded) {
    val statsData = screenState.stats
    val strings = resolveString { stats }

    val orientation = LocalOrientation.current

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
        modifier = Modifier.fillMaxSize()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp)
    ) {

        if (orientation == Orientation.Landscape) {
            item(span = StaggeredGridItemSpan.FullLine) { Spacer(Modifier.height(20.dp)) }
        }

        item(span = StaggeredGridItemSpan.FullLine) {
            Header(text = strings.todayTitle)
        }

        item {
            InfoCard(
                title = strings.formattedDuration(statsData.todayStats.timeSpent),
                subtitle = strings.timeSpentTitle
            )
        }

        item {
            InfoCard(
                title = statsData.todayStats.reviews.toString(),
                subtitle = strings.reviewsCountTitle
            )
        }

        val monthStats = screenState.stats.monthStats
        val selectedMonth = monthStats.selectedTimePeriodState.value

        item(
            span = StaggeredGridItemSpan.FullLine
        ) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val text = when {
                    monthStats.currentTimePeriod == selectedMonth -> {
                        strings.monthTitle
                    }

                    else -> {
                        strings.monthLabel(selectedMonth.monthStart)
                    }
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val previousMonthStart = selectedMonth.monthStart
                            .minus(1, DateTimeUnit.MONTH)
                        monthStats.selectedTimePeriodState.value = YearMonth(previousMonthStart)
                    }
                ) {
                    Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, null)
                }
                IconButton(onClick = {
                    val nextMonthStart = selectedMonth.monthStart
                        .plus(1, DateTimeUnit.MONTH)
                    monthStats.selectedTimePeriodState.value = YearMonth(nextMonthStart)
                }) {
                    Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, null)
                }
            }
        }


        item(span = StaggeredGridItemSpan.FullLine) {
            StatsMonthCalendar(
                today = statsData.todayStats.date,
                monthStats = monthStats
            )
        }

        val yearStats = screenState.stats.yearStats
        val selectedYear = yearStats.selectedTimePeriodState.value
        val yearDaysCount = LocalDate(selectedYear + 1, 1, 1)
            .minus(1, DateTimeUnit.DAY)
            .dayOfYear

        item(span = StaggeredGridItemSpan.FullLine) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val text = when {
                    screenState.stats.todayStats.date.year == selectedYear -> strings.yearTitle
                    else -> selectedYear.toString()
                }
                Header(text = text)
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = {
                        yearStats.selectedTimePeriodState.value = (selectedYear - 1)
                    }
                ) {
                    Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, null)
                }
                IconButton(
                    onClick = {
                        yearStats.selectedTimePeriodState.value = (selectedYear + 1)
                    }
                ) {
                    Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, null)
                }
            }
        }


        item(span = StaggeredGridItemSpan.FullLine) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatsYearCalendar(
                    today = statsData.todayStats.date,
                    stats = statsData.yearStats
                )
                Text(
                    text = strings.yearDaysPracticedLabel(
                        yearStats.statsState.value.dateToReviewsMap.size,
                        yearDaysCount
                    )
                )
            }
        }

        item(span = StaggeredGridItemSpan.FullLine) {
            Header(text = strings.totalTitle)
        }

        item {
            InfoCard(
                title = strings.formattedDuration(statsData.totalStats.timeSpent),
                subtitle = strings.timeSpentTitle
            )
        }
        item {
            InfoCard(
                title = statsData.totalStats.reviews.toString(),
                subtitle = strings.reviewsCountTitle
            )
        }

        item {
            InfoCard(
                title = statsData.totalStats.uniqueLettersStudied.toString(),
                subtitle = strings.uniqueLettersReviewed
            )
        }

        item {
            InfoCard(
                title = statsData.totalStats.uniqueWordsStudied.toString(),
                subtitle = strings.uniqueWordsReviewed
            )
        }

        item(span = StaggeredGridItemSpan.FullLine) {
            Spacer(Modifier.height(20.dp))
        }

    }
}

@Composable
private fun Header(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun InfoCard(title: String, subtitle: String) {
    Card {
        Column(Modifier.padding(20.dp)) {
            AutoSizeText(
                text = title,
                style = MaterialTheme.typography.displayMedium
            )
            Text(subtitle)
        }
    }
}
