package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.ui

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import ua.syt0r.kanji.core.srs.DailyGoalConfiguration
import ua.syt0r.kanji.presentation.common.resources.string.LettersDashboardStrings
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.CustomRippleTheme
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.DailyIndicatorData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.DailyProgress

@Composable
fun LettersDashboardDailyLimitIndicator(
    data: DailyIndicatorData,
    updateConfiguration: (DailyGoalConfiguration) -> Unit
) {

    var shouldShowDialog by remember { mutableStateOf(false) }
    if (shouldShowDialog) {
        DailyGoalDialog(
            configuration = data.configuration,
            onDismissRequest = { shouldShowDialog = false },
            onUpdateConfiguration = {
                updateConfiguration(it)
                shouldShowDialog = false
            }
        )
    }

    CompositionLocalProvider(
        LocalRippleTheme provides CustomRippleTheme(
            colorProvider = { MaterialTheme.colorScheme.onSurface }
        )
    ) {
        TextButton(
            onClick = { shouldShowDialog = true },
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(0.9f)
            )
        ) {
            Text(
                text = getIndicatorMessage(
                    strings = resolveString { lettersDashboard },
                    data = data
                ),
                fontWeight = FontWeight.Light
            )
        }

    }

}

@Composable
private fun getIndicatorMessage(
    strings: LettersDashboardStrings,
    data: DailyIndicatorData
): AnnotatedString {
    return when (data.progress) {
        is DailyProgress.Disabled -> buildAnnotatedString {
            withStyle(SpanStyle(MaterialTheme.colorScheme.onSurface)) {
                append(strings.dailyIndicatorPrefix)
            }
            withStyle(SpanStyle(MaterialTheme.colorScheme.outline)) {
                append(strings.dailyIndicatorDisabled)
            }
        }

        is DailyProgress.Completed -> buildAnnotatedString {
            withStyle(SpanStyle(MaterialTheme.colorScheme.onSurface)) {
                append(strings.dailyIndicatorPrefix)
            }
            withStyle(SpanStyle(MaterialTheme.extraColorScheme.success)) {
                append(strings.dailyIndicatorCompleted)
            }
        }

        is DailyProgress.StudyAndReview -> buildAnnotatedString {
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

        is DailyProgress.StudyOnly -> buildAnnotatedString {
            withStyle(SpanStyle(MaterialTheme.colorScheme.onSurface)) {
                append(strings.dailyIndicatorPrefix)
            }
            withStyle(SpanStyle(MaterialTheme.extraColorScheme.success)) {
                append(strings.dailyIndicatorNew(data.progress.count))
            }
        }

        is DailyProgress.ReviewOnly -> buildAnnotatedString {
            withStyle(SpanStyle(MaterialTheme.colorScheme.onSurface)) {
                append(strings.dailyIndicatorPrefix)
            }
            withStyle(SpanStyle(MaterialTheme.colorScheme.primary)) {
                append(strings.dailyIndicatorReview(data.progress.count))
            }
        }

    }
}
