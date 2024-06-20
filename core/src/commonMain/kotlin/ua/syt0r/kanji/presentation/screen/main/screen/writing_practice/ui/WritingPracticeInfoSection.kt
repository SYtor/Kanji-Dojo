package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.resolveString
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.trackItemPosition
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.common.ui.MostlySingleLineEliminateOverflowRow
import ua.syt0r.kanji.presentation.common.ui.kanji.Kanji
import ua.syt0r.kanji.presentation.common.ui.kanji.RadicalKanji
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterInputState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.KanaVoiceAutoPlayToggle
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.MultipleStrokeInputContentState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingReviewCharacterDetails
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingReviewState
import kotlin.math.min

private const val NoTranslationLayoutPreviewWordsLimit = 5

data class WritingPracticeInfoSectionData(
    val characterData: WritingReviewCharacterDetails,
    val kanaSoundAutoPlay: State<Boolean>,
    val isStudyMode: Boolean,
    val isCharacterDrawn: Boolean,
    val shouldHighlightRadicals: Boolean,
    val isNoTranslationLayout: Boolean
)

@Composable
fun State<WritingReviewState>.asInfoSectionState(
    noTranslationsLayout: Boolean,
    radicalsHighlight: State<Boolean>,
    kanaSoundAutoPlay: State<Boolean>
): State<WritingPracticeInfoSectionData> {
    return remember {
        derivedStateOf {
            val writerState = value.writerState
            when (val currentState = writerState.inputState) {
                is CharacterInputState.MultipleStroke -> {
                    WritingPracticeInfoSectionData(
                        characterData = value.characterDetails,
                        isStudyMode = false,
                        isCharacterDrawn = currentState.contentState.value is MultipleStrokeInputContentState.Processed,
                        shouldHighlightRadicals = radicalsHighlight.value,
                        isNoTranslationLayout = noTranslationsLayout,
                        kanaSoundAutoPlay = kanaSoundAutoPlay
                    )
                }

                is CharacterInputState.SingleStroke -> {
                    WritingPracticeInfoSectionData(
                        characterData = value.characterDetails,
                        isStudyMode = currentState.isStudyMode,
                        isCharacterDrawn = currentState.drawnStrokesCount.value == writerState.strokes.size,
                        shouldHighlightRadicals = radicalsHighlight.value,
                        isNoTranslationLayout = noTranslationsLayout,
                        kanaSoundAutoPlay = kanaSoundAutoPlay
                    )
                }
            }
        }
    }
}

private val MaxTransitionSlideDistance = 200.dp

@Composable
fun WritingPracticeInfoSection(
    state: State<WritingPracticeInfoSectionData>,
    modifier: Modifier = Modifier,
    bottomSheetHeight: MutableState<Dp>,
    onExpressionsClick: () -> Unit,
    toggleAutoPlay: () -> Unit,
    toggleRadicalsHighlight: () -> Unit,
    speakKana: (KanaReading) -> Unit,
    extraBottomPaddingState: State<Dp> = rememberUpdatedState(0.dp)
) {

    val transition = updateTransition(
        targetState = state.value,
        label = "Content Change Transition"
    )

    val density = LocalDensity.current

    transition.AnimatedContent(
        contentKey = { it.characterData.character to it.isStudyMode },
        modifier = modifier,
        transitionSpec = {
            val enterTransition = slideInHorizontally {
                min(it / 3, with(density) { MaxTransitionSlideDistance.roundToPx() })
            } + fadeIn()
            val exitTransition = slideOutHorizontally {
                -min(it / 3, with(density) { MaxTransitionSlideDistance.roundToPx() })
            } + fadeOut()
            enterTransition togetherWith exitTransition using SizeTransform(clip = false)
        }
    ) { data ->

        val scrollStateResetKey = data.run { characterData.character to isStudyMode }
        val scrollState = remember(scrollStateResetKey) { ScrollState(0) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            when (data.characterData) {
                is WritingReviewCharacterDetails.KanaReviewDetails -> {
                    KanaDetails(
                        details = data.characterData,
                        isStudyMode = data.isStudyMode,
                        autoPlay = data.kanaSoundAutoPlay,
                        toggleAutoPlay = toggleAutoPlay,
                        speakKana = speakKana
                    )
                }

                is WritingReviewCharacterDetails.KanjiReviewDetails -> {
                    KanjiDetails(
                        details = data.characterData,
                        isStudyMode = data.isStudyMode,
                        noTranslationsLayout = data.isNoTranslationLayout,
                        shouldHighlightRadicals = rememberUpdatedState(data.shouldHighlightRadicals),
                        toggleRadicalsHighlight = toggleRadicalsHighlight
                    )
                }
            }

            val expressions = data.characterData
                .run { if (data.isStudyMode || data.isCharacterDrawn) words else encodedWords }
                .takeIf { it.isNotEmpty() }

            if (expressions != null) {
                ExpressionsSection(
                    words = expressions,
                    isNoTranslationLayout = data.isNoTranslationLayout,
                    onClick = onExpressionsClick,
                    modifier = Modifier.trackItemPosition { data ->
                        if (transition.isRunning) return@trackItemPosition
                        bottomSheetHeight.value = data.heightFromScreenBottom
                            .takeIf { it > 200.dp }
                            ?: data.layoutCoordinates
                                .findRootCoordinates()
                                .size
                                .run { height / data.density.density }
                                .dp
                    }
                )
            }

            Spacer(modifier = Modifier.height(extraBottomPaddingState.value))

        }

    }

}

@Composable
private fun ColumnScope.KanaDetails(
    details: WritingReviewCharacterDetails.KanaReviewDetails,
    isStudyMode: Boolean,
    autoPlay: State<Boolean>,
    toggleAutoPlay: () -> Unit,
    speakKana: (KanaReading) -> Unit
) {

    if (isStudyMode) {
        Kanji(
            strokes = details.strokes,
            modifier = Modifier.size(80.dp).align(Alignment.CenterHorizontally)
        )
    }

    TextButton(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        onClick = { speakKana(details.reading) }
    ) {

        Text(
            text = buildAnnotatedString {
                append(details.kanaSystem.resolveString())
                append(" ")
                withStyle(MaterialTheme.typography.bodyLarge.toSpanStyle()) {
                    append(details.reading.nihonShiki.capitalize(Locale.current))
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(end = 8.dp)
        )

        Icon(Icons.Default.VolumeUp, null)

    }

    details.reading.alternative?.let { alternativeReadings ->
        Text(
            text = resolveString { commonPractice.additionalKanaReadingsNote(alternativeReadings) },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    KanaVoiceAutoPlayToggle(
        enabledState = autoPlay,
        enabled = true,
        onClick = toggleAutoPlay,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )

}

@Composable
private fun ColumnScope.KanjiDetails(
    details: WritingReviewCharacterDetails.KanjiReviewDetails,
    isStudyMode: Boolean,
    noTranslationsLayout: Boolean,
    shouldHighlightRadicals: State<Boolean>,
    toggleRadicalsHighlight: () -> Unit,
) {

    when {
        noTranslationsLayout -> {

            if (isStudyMode) {
                AnimatedKanjiSection(
                    strokes = details.strokes,
                    radicals = details.radicals,
                    shouldHighlightRadicals = shouldHighlightRadicals,
                    toggleRadicalsHighlight = toggleRadicalsHighlight,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

        }

        else -> {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                if (isStudyMode) {
                    AnimatedKanjiSection(
                        strokes = details.strokes,
                        radicals = details.radicals,
                        shouldHighlightRadicals = shouldHighlightRadicals,
                        toggleRadicalsHighlight = toggleRadicalsHighlight
                    )
                }

                KanjiMeanings(
                    meanings = details.meanings,
                    modifier = Modifier.weight(1f)
                )

            }
        }
    }

    details.on.takeIf { it.isNotEmpty() }?.let {
        KanjiReadingRow(
            title = resolveString { onyomi },
            readings = it
        )
    }

    details.kun.takeIf { it.isNotEmpty() }?.let {
        KanjiReadingRow(
            title = resolveString { kunyomi },
            readings = it
        )
    }

    if (details.variants != null) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(text = resolveString { writingPractice.variantsTitle })

            val showVariants = remember { mutableStateOf(false) }

            val overlayAlpha = animateFloatAsState(targetValue = if (showVariants.value) 0f else 1f)

            val overlayColor = MaterialTheme.colorScheme.surfaceVariant
                .copy(alpha = overlayAlpha.value)
            val hintTextColor = MaterialTheme.colorScheme.onSurface
                .copy(alpha = overlayAlpha.value)
            val variantsTextColor = MaterialTheme.colorScheme.onSurface
                .copy(alpha = 1f - overlayAlpha.value)

            Box(
                modifier = Modifier.clip(MaterialTheme.shapes.small)
                    .clickable { showVariants.value = !showVariants.value }
                    .background(overlayColor)
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Text(
                    text = resolveString { writingPractice.variantsHint },
                    color = hintTextColor,
                    maxLines = 1
                )
                Text(
                    text = details.variants,
                    color = variantsTextColor
                )
            }

        }

        val unicodeHex = String.format("U+%04X", details.character.first().code)
        Text(text = resolveString { writingPractice.unicodeTitle(unicodeHex) })

        Text(text = resolveString { writingPractice.strokeCountTitle(details.strokes.size) })

    }

}

@Composable
private fun AnimatedKanjiSection(
    strokes: List<Path>,
    radicals: List<CharacterRadical>,
    shouldHighlightRadicals: State<Boolean>,
    toggleRadicalsHighlight: () -> Unit,
    modifier: Modifier = Modifier
) {

    val radicalsTransition = updateTransition(
        targetState = shouldHighlightRadicals.value,
        label = "Radical highlight transition"
    )

    radicalsTransition.AnimatedContent(
        modifier = modifier
            .size(80.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = toggleRadicalsHighlight),
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { shouldHighlight ->

        when (shouldHighlight) {
            true -> RadicalKanji(
                strokes = strokes,
                radicals = radicals,
                modifier = Modifier.fillMaxSize()
            )

            false -> Kanji(
                strokes = strokes,
                modifier = Modifier.fillMaxSize()
            )
        }

    }

}

@Composable
private fun KanjiMeanings(
    meanings: List<String>,
    modifier: Modifier = Modifier
) {

    Column(modifier) {

        Text(
            text = meanings.firstOrNull()?.capitalize(Locale.current)
                ?: resolveString { writingPractice.noKanjiTranslationsLabel },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )

        if (meanings.size > 1) {
            MostlySingleLineEliminateOverflowRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                meanings.drop(1).forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

            }

        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KanjiReadingRow(
    title: String,
    readings: List<String>
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = title,
            modifier = Modifier
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            readings.forEach {

                Text(
                    text = it,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    maxLines = 1
                )

            }

        }

    }

}

@Composable
private fun ExpressionsSection(
    words: List<JapaneseWord>,
    isNoTranslationLayout: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 16.dp)
    ) {

        Text(
            text = resolveString { writingPractice.headerWordsMessage(words.size) },
            style = MaterialTheme.typography.titleLarge
        )

        Row(verticalAlignment = Alignment.Bottom) {

            MostlySingleLineEliminateOverflowRow(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {

                if (isNoTranslationLayout) {
                    words.take(NoTranslationLayoutPreviewWordsLimit).forEach {
                        FuriganaText(
                            furiganaString = it.readings.first(),
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                } else {
                    FuriganaText(
                        furiganaString = words.first().preview(),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }

            }

            IconButton(
                onClick = onClick,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, null)
            }

        }

    }

}
