package ua.syt0r.kanji.presentation.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.ClickableFuriganaText
import ua.syt0r.kanji.presentation.common.ui.FuriganaText


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlternativeWordsDialog(
    word: JapaneseWord,
    onDismissRequest: () -> Unit,
    onFuriganaClick: ((String) -> Unit)? = null,
    onFeedbackClick: (() -> Unit)? = null
) {

    val strings = resolveString { alternativeDialog }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = strings.title) },
        buttons = {
            onFeedbackClick?.let {
                TextButton(
                    onClick = it
                ) {
                    Text(
                        text = strings.reportButton,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Icon(Icons.Outlined.Flag, null)
                }
                Spacer(Modifier.weight(1f))
            }
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(text = strings.closeButton)
            }
        },
        content = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = strings.readingsTitle,
                    style = MaterialTheme.typography.titleMedium
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally
                    )
                ) {
                    word.readings.forEach { reading ->
                        if (onFuriganaClick != null) {
                            ClickableFuriganaText(
                                furiganaString = reading,
                                onClick = onFuriganaClick,
                                modifier = Modifier.align(Alignment.Bottom)
                            )
                        } else {
                            FuriganaText(
                                furiganaString = reading,
                                modifier = Modifier.align(Alignment.Bottom)
                            )
                        }
                    }
                }

                Text(
                    text = strings.meaningsTitle,
                    style = MaterialTheme.typography.titleMedium
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    word.meanings.forEach { text ->
                        Text(
                            text = text.capitalize(Locale.current),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Bottom)
                        )
                    }
                }

            }

        }
    )

}
