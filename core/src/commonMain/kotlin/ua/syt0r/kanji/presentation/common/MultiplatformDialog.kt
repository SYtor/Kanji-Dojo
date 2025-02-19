package ua.syt0r.kanji.presentation.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
expect fun MultiplatformDialog(
    onDismissRequest: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
)


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiplatformDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable RowScope.() -> Unit,
    contentVerticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    paddedContent: Boolean = true
) = ExperimentalMultiplatformDialog(
    onDismissRequest,
    title,
    content,
    buttons,
    contentVerticalArrangement,
    paddedContent
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExperimentalMultiplatformDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable FlowRowScope.() -> Unit,
    contentVerticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    paddedContent: Boolean = true
) {

    MultiplatformDialog(
        onDismissRequest = onDismissRequest
    ) {

        Column(
            modifier = Modifier
                .animateContentSize()
                .height(IntrinsicSize.Max)
                .padding(top = 20.dp, bottom = 10.dp)
        ) {

            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp)
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleLarge
                ) {
                    title()
                }
            }

            val contentScrollState = rememberScrollState()

            val visibleDividerColor = MaterialTheme.colorScheme.outlineVariant
            val hiddenDividerColor = MaterialTheme.colorScheme.surface

            val topDividerColor = animateColorAsState(
                targetValue = when {
                    contentScrollState.canScrollBackward -> visibleDividerColor
                    else -> hiddenDividerColor
                }
            )

            HorizontalDivider(color = topDividerColor.value)

            Column(
                modifier = Modifier
                    .padding(horizontal = if (paddedContent) 20.dp else 0.dp)
                    .weight(1f)
                    .verticalScroll(contentScrollState),
                verticalArrangement = contentVerticalArrangement
            ) {
                content()
            }

            val bottomDividerColor = animateColorAsState(
                targetValue = when {
                    contentScrollState.canScrollForward -> visibleDividerColor
                    else -> hiddenDividerColor
                }
            )

            HorizontalDivider(color = bottomDividerColor.value)

            FlowRow(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp)
            ) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                    buttons()
                }
            }

        }

    }

}