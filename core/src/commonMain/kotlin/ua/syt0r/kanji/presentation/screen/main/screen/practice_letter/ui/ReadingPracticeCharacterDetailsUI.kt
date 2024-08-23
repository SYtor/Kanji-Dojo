package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.resolveString
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.textDp
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiReadingsContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.KanaVoiceAutoPlayToggle
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState

@Composable
fun ReadingPracticeCharacterDetailsUI(
    state: LetterPracticeReviewState.Reading,
    speakKana: (KanaReading) -> Unit,
    modifier: Modifier
) {

    val revealed = rememberUpdatedState(state.revealed.value)
    val alpha = remember { derivedStateOf { if (revealed.value) 1f else 0f } }
    val clickable by remember { derivedStateOf { alpha.value == 1f } }

    val arrangement: Arrangement.Vertical
    val characterDetailsContent: @Composable ColumnScope.() -> Unit

    when (state.itemData) {

        is LetterPracticeItemData.KanaReadingData -> {
            arrangement = Arrangement.spacedBy(0.dp)
            characterDetailsContent = {
                KanaDetails(
                    data = state.itemData,
                    kanaAutoPlay = state.layout.kanaAutoPlay,
                    toggleKanaAutoPlay = {
                        state.layout.run { kanaAutoPlay.value = !kanaAutoPlay.value }
                    },
                    speakKana = speakKana,
                    alpha = alpha,
                    clickable = clickable
                )
            }
        }

        is LetterPracticeItemData.KanjiReadingData -> {
            arrangement = Arrangement.spacedBy(8.dp)
            characterDetailsContent = {
                KanjiDetails(
                    data = state.itemData,
                    alpha = alpha,
                    clickable = clickable
                )
            }
        }

    }

    Column(
        modifier = modifier,
        verticalArrangement = arrangement
    ) {

        Text(
            text = state.itemData.character,
            fontSize = 80.textDp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .padding(bottom = 20.dp)
        )

        characterDetailsContent()

    }

}

@Composable
private fun ColumnScope.KanaDetails(
    data: LetterPracticeItemData.KanaReadingData,
    kanaAutoPlay: State<Boolean>,
    toggleKanaAutoPlay: () -> Unit,
    speakKana: (reading: KanaReading) -> Unit,
    alpha: State<Float>,
    clickable: Boolean
) {

    KanaVoiceAutoPlayToggle(
        enabledState = kanaAutoPlay,
        onClick = toggleKanaAutoPlay,
        enabled = clickable,
        modifier = Modifier.align(Alignment.CenterHorizontally).alpha(alpha.value)
    )

    TextButton(
        onClick = { speakKana(data.reading) },
        enabled = clickable,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.align(Alignment.CenterHorizontally).alpha(alpha.value),
    ) {

        Text(
            text = buildAnnotatedString {
                append(data.kanaSystem.resolveString())
                append(" ")
                withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                    append(data.reading.nihonShiki.capitalize(Locale.current))
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(end = 8.dp)
        )

        Icon(Icons.AutoMirrored.Filled.VolumeUp, null)

    }

    data.reading.alternative?.let { alternativeReadings ->
        Text(
            text = resolveString { commonPractice.additionalKanaReadingsNote(alternativeReadings) },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally).alpha(alpha.value)
        )
    }

    Spacer(Modifier.height(8.dp))

}

@Composable
private fun ColumnScope.KanjiDetails(
    data: LetterPracticeItemData.KanjiReadingData,
    alpha: State<Float>,
    clickable: Boolean,
) {

    if (data.meanings.isNotEmpty()) {
        Text(
            text = data.meanings.joinToString(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.alpha(alpha.value)
        )
    }

    KanjiReadingsContainer(
        on = data.on,
        kun = data.kun,
        modifier = Modifier.fillMaxWidth().alpha(alpha.value)
    )

    if (data.variants != null) {
        KanjiVariantsRow(
            data.variants,
            modifier = Modifier.alpha(alpha.value)
        )
    }

}
