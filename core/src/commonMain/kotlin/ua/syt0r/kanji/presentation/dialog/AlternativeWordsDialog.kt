package ua.syt0r.kanji.presentation.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.buildFuriganaString
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.ClickableFuriganaText
import ua.syt0r.kanji.presentation.common.ui.FuriganaText


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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text(
                    text = strings.readingsTitle,
                    style = MaterialTheme.typography.titleMedium
                )

                word.readings.forEachIndexed { index, reading ->

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        Text("${index + 1}. ", Modifier.alignByBaseline())

                        /*
                         * Align by baseline has issues when aligning text that starts with inline
                         * content, adding non empty string in the beginning to fix this behaviour
                         */
                        val fixedFurigana = buildFuriganaString {
                            append(" ")
                            append(reading)
                        }

                        if (onFuriganaClick != null) {
                            ClickableFuriganaText(
                                furiganaString = fixedFurigana,
                                onClick = onFuriganaClick,
                                modifier = Modifier.alignByBaseline()
                            )
                        } else {
                            FuriganaText(
                                furiganaString = fixedFurigana,
                                modifier = Modifier.alignByBaseline()
                            )
                        }
                    }

                }

                Text(
                    text = strings.meaningsTitle,
                    style = MaterialTheme.typography.titleMedium
                )

                word.meanings.forEachIndexed { index, text ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("${index + 1}.  ", Modifier.alignByBaseline())
                        Text(
                            text = text.capitalize(Locale.current)
                        )
                    }
                }

            }

        }
    )

}
