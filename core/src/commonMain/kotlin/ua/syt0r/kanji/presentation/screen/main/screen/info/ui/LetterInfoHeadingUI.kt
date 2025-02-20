package ua.syt0r.kanji.presentation.screen.main.screen.info.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.resolveString
import ua.syt0r.kanji.presentation.common.resources.icon.Copy
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.kanji.AnimatedKanji
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiBackground
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiRadicalsSection
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiReadingsContainer
import ua.syt0r.kanji.presentation.screen.main.screen.info.LetterInfoData

@Composable
fun LetterInfoHeadingUI(
    letterData: LetterInfoData,
    onCopyButtonClick: () -> Unit,
    onRadicalClick: (String) -> Unit
) {

    when (letterData) {
        is LetterInfoData.Kana -> {
            KanaInfo(
                data = letterData,
                onCopyButtonClick = onCopyButtonClick,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
        }

        is LetterInfoData.Kanji -> {
            KanjiInfo(
                data = letterData,
                onCopyButtonClick = onCopyButtonClick,
                onRadicalClick = onRadicalClick,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
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

                OutlinedIconButton(
                    onClick = onCopyButtonClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(ExtraIcons.Copy, null)
                }

            }

        }
    }

}

@Composable
private fun KanjiInfo(
    data: LetterInfoData.Kanji,
    onCopyButtonClick: () -> Unit,
    onRadicalClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            AnimatableCharacter(strokes = data.strokes)

            Column(
                modifier = Modifier.weight(1f)
            ) {

                data.grade?.let {
                    Text(
                        text = resolveString { info.gradeMessage(it) },
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                data.jlptLevel?.let {
                    Text(
                        text = resolveString { info.jlptMessage(it) },
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                data.frequency?.let {
                    Text(
                        text = resolveString { info.frequencyMessage(it) },
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                OutlinedIconButton(
                    onClick = onCopyButtonClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(ExtraIcons.Copy, null)
                }

            }

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

        KanjiRadicalsSection(
            state = data.radicalsSectionData,
            onRadicalClick = onRadicalClick
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
