package ua.syt0r.kanji.presentation.screen.main.screen.info.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import ua.syt0r.kanji.presentation.common.resolveString
import ua.syt0r.kanji.presentation.common.resources.icon.Copy
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.kanji.AnimatedKanji
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiBackground
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiReadingsContainer
import ua.syt0r.kanji.presentation.screen.main.screen.info.LetterInfoData

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LetterInfoKanaHeading(data: LetterInfoData.Kana) {

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            AnimatableCharacter(data.strokes)

            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                val readings = data.reading.let {
                    if (it.alternative != null) listOf(it.nihonShiki) + it.alternative
                    else listOf(it.nihonShiki)
                }

                val messages = listOf(
                    data.kanaSystem.resolveString(),
                    resolveString { info.romajiMessage(readings) }
                )

                Text(
                    text = messages.joinToString("\n"),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall
                )

                CopyButton(data.character)

            }

        }

    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LetterInfoKanjiHeading(data: LetterInfoData.Kanji) {

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
        ) {

            AnimatableCharacter(strokes = data.strokes)

            Column(
                modifier = Modifier.weight(1f)
            ) {

                val messages = listOfNotNull(
                    data.grade?.let { resolveString { info.gradeMessage(it) } },
                    data.jlptLevel?.let { resolveString { info.jlptMessage(it) } },
                    data.frequency?.let { resolveString { info.frequencyMessage(it) } }
                )

                if (messages.isNotEmpty()) {
                    Text(
                        text = messages.joinToString("\n"),
                        style = MaterialTheme.typography.titleSmall
                    )
                }

            }

            CopyButton(data.character)

        }

        if (data.meanings.isNotEmpty()) {
            Text(
                text = data.meanings.joinToString(),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        KanjiReadingsContainer(
            on = data.on,
            kun = data.kun,
            modifier = Modifier.fillMaxWidth()
        )

    }

}

@Composable
private fun AnimatableCharacter(strokes: List<Path>) {

    Column {

        Card(
            modifier = Modifier.size(120.dp),
            elevation = CardDefaults.elevatedCardElevation()
        ) {

            Box(modifier = Modifier.fillMaxSize()) {

                KanjiBackground(Modifier.fillMaxSize())

                AnimatedKanji(
                    strokes = strokes,
                    modifier = Modifier.fillMaxSize()
                )

            }

        }

        Text(
            text = resolveString { info.strokesMessage(strokes.size) },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(top = 4.dp)
                .align(Alignment.CenterHorizontally)
        )

    }

}

private const val CopyAnimationDuration = 800L

@Composable
private fun CopyButton(
    copyData: String,
    modifier: Modifier = Modifier
) {

    val clipboardManager = LocalClipboardManager.current
    var copying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        snapshotFlow { copying }
            .filter { it }
            .collect {
                delay(CopyAnimationDuration)
                copying = false
            }
    }

    IconButton(
        onClick = {
            clipboardManager.setText(AnnotatedString(copyData))
            copying = true
        },
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = copying,
            transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() }
        ) {
            if (it) Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .background(MaterialTheme.extraColorScheme.success, CircleShape)
                    .size(24.dp)
                    .padding(2.dp)
            )
            else Icon(ExtraIcons.Copy, null)
        }
    }

}
