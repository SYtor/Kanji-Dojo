package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.CustomRippleTheme
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.DailyIndicatorData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.practice_dashboard.DailyProgress

@Composable
fun PracticeDashboardDailyLimitIndicator(
    state: State<DailyIndicatorData?>,
    updateConfiguration: (DailyGoalConfiguration) -> Unit
) {
    CompositionLocalProvider(
        LocalRippleTheme provides CustomRippleTheme(
            colorProvider = { MaterialTheme.colorScheme.onSurface }
        )
    ) {

        val cachedData = remember { mutableStateOf<DailyIndicatorData?>(null) }

        LaunchedEffect(Unit) {
            snapshotFlow { state.value }
                .filterNotNull()
                .onEach { cachedData.value = it }
                .collect()
        }

        val data = cachedData.value
        val strings = resolveString { practiceDashboard }
        val message = when {
            data == null -> null
            data.progress is DailyProgress.Disabled -> buildAnnotatedString {
                withStyle(SpanStyle(MaterialTheme.colorScheme.onSurface)) {
                    append(strings.dailyIndicatorPrefix)
                }
                withStyle(SpanStyle(MaterialTheme.colorScheme.outline)) {
                    append(strings.dailyIndicatorDisabled)
                }
            }

            data.progress is DailyProgress.Completed -> buildAnnotatedString {
                withStyle(SpanStyle(MaterialTheme.colorScheme.onSurface)) {
                    append(strings.dailyIndicatorPrefix)
                }
                withStyle(SpanStyle(MaterialTheme.extraColorScheme.success)) {
                    append(strings.dailyIndicatorCompleted)
                }
            }

            data.progress is DailyProgress.StudyAndReview -> buildAnnotatedString {
                withStyle(SpanStyle(MaterialTheme.colorScheme.onSurface)) {
                    append(strings.dailyIndicatorPrefix)
                }
                withStyle(SpanStyle(MaterialTheme.extraColorScheme.success)) {
                    append(strings.dailyIndicatorNew(data.progress.study))
                }
                withStyle(SpanStyle(MaterialTheme.colorScheme.onSurface)) {
                    append(" • ")
                }
                withStyle(SpanStyle(MaterialTheme.colorScheme.primary)) {
                    append(strings.dailyIndicatorReview(data.progress.review))
                }
            }

            data.progress is DailyProgress.StudyOnly -> buildAnnotatedString {
                withStyle(SpanStyle(MaterialTheme.colorScheme.onSurface)) {
                    append(strings.dailyIndicatorPrefix)
                }
                withStyle(SpanStyle(MaterialTheme.extraColorScheme.success)) {
                    append(strings.dailyIndicatorNew(data.progress.count))
                }
            }

            data.progress is DailyProgress.ReviewOnly -> buildAnnotatedString {
                withStyle(SpanStyle(MaterialTheme.colorScheme.onSurface)) {
                    append(strings.dailyIndicatorPrefix)
                }
                withStyle(SpanStyle(MaterialTheme.colorScheme.primary)) {
                    append(strings.dailyIndicatorReview(data.progress.count))
                }
            }

            else -> throw IllegalStateException()
        }

        val alpha = animateFloatAsState(
            targetValue = if (message != null) 1f else 0f
        )

        var shouldShowDialog by remember { mutableStateOf(false) }
        if (shouldShowDialog) {
            DailyGoalDialog(
                configuration = data!!.configuration,
                onDismissRequest = { shouldShowDialog = false },
                onUpdateConfiguration = {
                    updateConfiguration(it)
                    shouldShowDialog = false
                }
            )
        }

        TextButton(
            onClick = { shouldShowDialog = true },
            modifier = Modifier.fillMaxWidth().wrapContentSize().alpha(alpha.value)
        ) {
            Text(
                text = message ?: AnnotatedString("Placeholder"),
                fontWeight = FontWeight.Light
            )
        }

    }
}