package ua.syt0r.kanji.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@Composable
fun MultiplatformDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    buttons: @Composable RowScope.() -> Unit,
    contentVerticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(0.dp),
    scrollableContent: Boolean = true,
    paddedContent: Boolean = true
) {

    MultiplatformDialog(
        onDismissRequest = onDismissRequest
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .let { if (scrollableContent) it.verticalScroll(rememberScrollState()) else it }
                .padding(top = 20.dp, bottom = 10.dp)
        ) {

            Box(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.titleLarge
                ) {
                    title()
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier.padding(horizontal = if (paddedContent) 20.dp else 0.dp),
                verticalArrangement = contentVerticalArrangement
            ) {
                content()
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 20.dp)
                    .align(Alignment.End)
            ) { buttons() }

        }

    }

}