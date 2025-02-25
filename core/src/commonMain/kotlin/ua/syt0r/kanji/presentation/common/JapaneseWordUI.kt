package ua.syt0r.kanji.presentation.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.VocabReading
import ua.syt0r.kanji.core.app_data.data.formattedVocabDefinition
import ua.syt0r.kanji.presentation.common.ui.ClickableFuriganaText
import ua.syt0r.kanji.presentation.common.ui.FuriganaText

@Composable
fun JapaneseWordUI(
    index: Int,
    onClick: (() -> Unit)? = null,
    addWordToVocabDeckClick: (() -> Unit)? = null,
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {

    val trailingContent: @Composable (() -> Unit)?

    if (addWordToVocabDeckClick != null) {
        trailingContent = {
            IconButton(
                onClick = addWordToVocabDeckClick
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    } else {
        trailingContent = null
    }

    ListItem(
        modifier = modifier
//            .clip(MaterialTheme.shapes.large)
            .clickable(onClick),
        leadingContent = { Text((index + 1).toString()) },
        headlineContent = headline,
        trailingContent = trailingContent
    )

}

@Composable
fun JapaneseWordUI(
    index: Int,
    word: JapaneseWord,
    onClick: (() -> Unit)? = null,
    onFuriganaClick: ((String) -> Unit)? = null,
    addWordToVocabDeckClick: (() -> Unit)? = null,
    headline: @Composable () -> Unit = {
        FuriganaWordHeadline(word.reading, word.combinedGlossary(), onFuriganaClick)
    },
    modifier: Modifier = Modifier
) = JapaneseWordUI(
    index = index,
    onClick = onClick,
    addWordToVocabDeckClick = addWordToVocabDeckClick,
    headline = headline,
    modifier = modifier
)

@Composable
fun FuriganaWordHeadline(
    reading: VocabReading,
    glossary: String,
    onFuriganaClick: ((String) -> Unit)? = null
) {
    val furigana = formattedVocabDefinition(reading, glossary)
    if (onFuriganaClick != null) ClickableFuriganaText(furigana, onFuriganaClick)
    else FuriganaText(furigana)
}


@Composable
fun NewStyleJapaneseWordUI(
    index: Int,
    partOfSpeechList: List<String> = emptyList(),
    onClick: (() -> Unit)? = null,
    addWordToVocabDeckClick: (() -> Unit)? = null,
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {

    NewStyleLayout(
        modifier = modifier.clickable(onClick),
        topContent = {
            Text(
                text = "${index + 1}",
                modifier = Modifier.weight(1f)
            )

            addWordToVocabDeckClick?.let {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable(it)
                        .height(30.dp)
                        .padding(6.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        },
        middleContent = { headline() },
        bottomContent = {
            Text(
                text = partOfSpeechList.joinToString(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    )

}

@Composable
private fun NewStyleLayout(
    modifier: Modifier,
    topContent: @Composable RowScope.() -> Unit,
    middleContent: @Composable RowScope.() -> Unit,
    bottomContent: @Composable RowScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start)
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
