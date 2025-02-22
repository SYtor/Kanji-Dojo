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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiRadicalUI
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiRadicalsSectionData
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiReadingsContainer
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenExpandableSection
import ua.syt0r.kanji.presentation.screen.main.screen.info.LetterInfoData

@Composable
fun LetterInfoHeadingUI(
    letterData: LetterInfoData,
    onRadicalClick: (String) -> Unit
) {

    val clipboardManager = LocalClipboardManager.current
    val onCopyButtonClick = { clipboardManager.setText(AnnotatedString(letterData.character)) }

    when (letterData) {
        is LetterInfoData.Kana -> {
            KanaInfo(
                data = letterData,
                onCopyButtonClick = onCopyButtonClick,
                modifier = Modifier.fillMaxWidth()
            )
        }

        is LetterInfoData.Kanji -> {
            Column {
                KanjiInfo(
                    data = letterData,
                    onCopyButtonClick = onCopyButtonClick,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                )

                val radicalsExpanded = rememberSaveable { mutableStateOf(true) }

                ExpandableKanjiRadicalsSection(
                    expanded = radicalsExpanded,
                    data = letterData.radicalsSectionData,
                    onRadicalClick = onRadicalClick
                )
            }
        }
    }

}

@Composable
private fun KanaInfo(
    data: LetterInfoData.Kana,
    onCopyButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            AnimatableCharacter(data.strokes)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = data.kanaSystem.resolveString(),
                    style = MaterialTheme.typography.headlineSmall
                )

                val readings = data.reading.let {
                    if (it.alternative != null) listOf(it.nihonShiki) + it.alternative
                    else listOf(it.nihonShiki)
                }

                Text(
                    text = resolveString { info.romajiMessage(readings) },
                    style = MaterialTheme.typography.headlineSmall
                )

                CopyButton(onCopyButtonClick, Modifier.align(Alignment.End))

            }

        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KanjiInfo(
    data: LetterInfoData.Kanji,
    onCopyButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.padding(horizontal = 20.dp),
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

            CopyButton(
                onCopyButtonClick = onCopyButtonClick
            )

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
    onCopyButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {

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
            onCopyButtonClick()
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


@Composable
private fun ExpandableKanjiRadicalsSection(
    expanded: MutableState<Boolean>,
    data: KanjiRadicalsSectionData,
    onRadicalClick: (String) -> Unit,
) {

    InfoScreenExpandableSection(
        expanded = expanded,
        header = { Text("Radicals (${data.radicals.size})") },
        expandedContent = {
            data.radicals.forEach {
                KanjiRadicalUI(
                    strokes = data.strokes,
                    radicalDetails = it,
                    onRadicalClick = onRadicalClick
                )
            }
        }
    )

}