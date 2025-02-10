package ua.syt0r.kanji.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.VocabReading
import ua.syt0r.kanji.core.app_data.data.buildFuriganaString
import ua.syt0r.kanji.core.app_data.data.withEncodedText
import ua.syt0r.kanji.presentation.common.ui.ClickableFuriganaText
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AddWordToDeckDialog

@Composable
fun JapaneseWordUI(
    index: Int,
    word: JapaneseWord,
    onClick: (() -> Unit)? = null,
    onFuriganaClick: ((String) -> Unit)? = null,
    addWordToVocabDeckClick: (() -> Unit)? = null,
    headline: @Composable () -> Unit = { FuriganaWordHeadline(word, onFuriganaClick) },
    modifier: Modifier = Modifier
) {

    var showAddToVocabDeckDialog by rememberSaveable(index) { mutableStateOf(false) }
    if (showAddToVocabDeckDialog) {
        AddWordToDeckDialog(
            word = word,
            onDismissRequest = { showAddToVocabDeckDialog = false }
        )
    }

    Column {

        if (index != 0) HorizontalDivider()

        JapaneseWordUILayout(
            modifier = modifier.clickable(onClick),
            topContent = {
                Text(
                    text = "${index + 1}",
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .sizeIn(minWidth = 20.dp)
                        .wrapContentSize()
                )
            },
            middleContent = { headline() },
            bottomContent = {

                Text(
                    text = word.partOfSpeechList.joinToString { it.name },
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                addWordToVocabDeckClick?.let {
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable { showAddToVocabDeckDialog = true }
                            .height(30.dp)
                            .padding(6.dp)
                    ) {
                        Icon(Icons.Default.Add, null)
                    }
                }

            }
        )

    }

}

@Composable
fun JapaneseWordUILayout(
    modifier: Modifier,
    topContent: @Composable RowScope.() -> Unit,
    middleContent: @Composable RowScope.() -> Unit,
    bottomContent: @Composable RowScope.() -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                LocalTextStyle provides MaterialTheme.typography.bodySmall
            ) {

                topContent()

            }
        }
        Row {
            middleContent()
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                LocalTextStyle provides MaterialTheme.typography.bodySmall
            ) {
                bottomContent()
            }
        }

    }
}

@Composable
fun FuriganaWordHeadline(word: JapaneseWord, onFuriganaClick: ((String) -> Unit)?) {
    val furigana = buildFuriganaString {
        when (val reading = word.displayReading) {
            is VocabReading.Kana -> {
                append(reading.reading)
            }

            is VocabReading.Kanji -> {
                if (reading.furigana != null) {
                    append(reading.furigana)
                } else {
                    append("${reading.kanjiReading}【${reading.kanaReading}】")
                }
            }
        }
        append("・ ")
        append(word.combinedGlossary())
    }

    if (onFuriganaClick != null) ClickableFuriganaText(furigana, onFuriganaClick)
    else FuriganaText(furigana)
}

@Composable
fun HiddenLetterWordHeadline(word: JapaneseWord, letterToHide: String) {
    val furigana = buildFuriganaString {
        append(word.displayReading.furiganaPreview.withEncodedText(letterToHide))
        append("・ ")
        append(word.combinedGlossary())
    }
    FuriganaText(furigana)
}