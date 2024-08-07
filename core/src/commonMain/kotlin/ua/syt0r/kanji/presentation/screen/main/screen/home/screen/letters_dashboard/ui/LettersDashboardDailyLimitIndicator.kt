package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.resources.string.LettersDashboardStrings
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.CustomRippleTheme
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.DailyIndicatorData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.DailyProgress

@Composable
fun LettersDashboardDailyLimitIndicator(
    data: DailyIndicatorData,
    onIndicatorClick: () -> Unit
) {

    CompositionLocalProvider(
        LocalRippleTheme provides CustomRippleTheme(
            colorProvider = { MaterialTheme.colorScheme.onSurface }
        )
    ) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp)
                .clip(ButtonDefaults.textShape)
                .clickable(onClick = onIndicatorClick)
                .padding(ButtonDefaults.TextButtonWithIconContentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = getIndicatorMessage(
                    strings = resolveString { lettersDashboard },
                    data = data
                ),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Light
            )

            Icon(Icons.Outlined.Settings, null)

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
