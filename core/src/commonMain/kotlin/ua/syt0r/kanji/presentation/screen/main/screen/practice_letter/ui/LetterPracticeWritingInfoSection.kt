package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.toFurigana
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.presentation.common.ItemPositionData
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.trackItemPosition
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.common.ui.MostlySingleLineEliminateOverflowRow
import ua.syt0r.kanji.presentation.common.ui.kanji.Kanji
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiReadingsContainer
import ua.syt0r.kanji.presentation.common.ui.kanji.RadicalKanji
import ua.syt0r.kanji.presentation.common.ui.kanji.getColoredKanjiStrokes
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWritingProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeLayoutConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState
import kotlin.math.min

private const val NoTranslationLayoutPreviewWordsLimit = 5

data class WritingPracticeInfoSectionData(
    val characterData: LetterPracticeItemData.WritingData,
    val isStudyMode: Boolean,
    val revealCharacter: Boolean,
    val layoutConfiguration: LetterPracticeLayoutConfiguration.WritingLayoutConfiguration
)

@Composable
fun State<LetterPracticeReviewState.Writing>.asInfoSectionState(
    layoutConfiguration: LetterPracticeLayoutConfiguration.WritingLayoutConfiguration
): State<WritingPracticeInfoSectionData> {
    return remember {
        derivedStateOf {
            val currentState = value
            val writerState = currentState.writerState.value
            val revealCharacter = writerState.progress.value !is CharacterWritingProgress.Writing

            when (val configuration = writerState.configuration) {
                CharacterWriterConfiguration.CharacterInput -> {
                    WritingPracticeInfoSectionData(
                        characterData = currentState.itemData,
                        isStudyMode = false,
                        revealCharacter = revealCharacter,
                        layoutConfiguration = layoutConfiguration
                    )
                }

                is CharacterWriterConfiguration.StrokeInput -> {
                    WritingPracticeInfoSectionData(
                        characterData = currentState.itemData,
                        isStudyMode = configuration.isStudyMode,
                        revealCharacter = revealCharacter,
                        layoutConfiguration = layoutConfiguration
                    )
                }
            }
        }
    }
}

private val MaxTransitionSlideDistance = 200.dp

@Composable
fun LetterPracticeWritingInfoSection(
    state: State<WritingPracticeInfoSectionData>,
    modifier: Modifier = Modifier,
    bottomSheetHeight: MutableState<Dp>,
    onExpressionsClick: () -> Unit,
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
    ) { currentSectionData ->

        val scrollStateResetKey = currentSectionData.run { characterData.character to isStudyMode }
        val scrollState = remember(scrollStateResetKey) { ScrollState(0) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            when (currentSectionData.characterData) {
                is LetterPracticeItemData.KanaWritingData -> {
                    val autoPlay = currentSectionData.layoutConfiguration.kanaAutoPlay
                    KanaDetails(
                        details = currentSectionData.characterData,
                        isStudyMode = currentSectionData.isStudyMode,
                        autoPlay = autoPlay,
                        toggleAutoPlay = { autoPlay.value = autoPlay.value.not() },
                        speakKana = speakKana
                    )
                }

                is LetterPracticeItemData.KanjiWritingData -> {
                    val highlightRadicals = currentSectionData.layoutConfiguration.radicalsHighlight
                    KanjiDetails(
                        details = currentSectionData.characterData,
                        isStudyMode = currentSectionData.isStudyMode,
                        noTranslationsLayout = currentSectionData.layoutConfiguration.noTranslationsLayout,
                        shouldHighlightRadicals = highlightRadicals,
                        toggleRadicalsHighlight = {
                            highlightRadicals.value = highlightRadicals.value.not()
                        }
                    )
                }
            }

            val expressions = currentSectionData.characterData.run {
                if (currentSectionData.isStudyMode || currentSectionData.revealCharacter) words else encodedWords
            }
                .takeIf { it.isNotEmpty() }

            val animatedVocabPos = remember { mutableStateOf<ItemPositionData?>(null) }
            if (expressions != null) {
                ExpressionsSection(
                    words = expressions,
                    isNoTranslationLayout = currentSectionData.layoutConfiguration.noTranslationsLayout,
                    onClick = onExpressionsClick,
                    modifier = Modifier.trackItemPosition { animatedVocabPos.value = it }
                )
            }

            LaunchedEffect(currentSectionData) {
                val itemPositionData = snapshotFlow { transition.targetState }
                    .filter { it == currentSectionData }
                    .flatMapLatest {
                        snapshotFlow { animatedVocabPos.value }.filterNotNull()
                    }
                    .first()

                val vocabSheetHeight = itemPositionData.heightFromScreenBottom
                    .takeIf { it > 200.dp }
                    ?: itemPositionData.layoutCoordinates
                        .findRootCoordinates()
                        .size
                        .run { height / itemPositionData.density.density }
                        .dp
                Logger.d("changing height to ${vocabSheetHeight.value} ${itemPositionData.layoutCoordinates.positionInParent().y}")
                bottomSheetHeight.value = vocabSheetHeight
            }

            Spacer(modifier = Modifier.height(extraBottomPaddingState.value))

        }

    }

}

@Composable
private fun ColumnScope.KanaDetails(
    details: LetterPracticeItemData.KanaWritingData,
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

    LetterPracticeKanaInfo(
        kanaSystem = details.kanaSystem,
        reading = details.reading,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )

    KanaVoiceMenu(
        autoPlayEnabled = autoPlay,
        clickable = true,
        onAutoPlayToggleClick = toggleAutoPlay,
        onSpeakClick = { speakKana(details.reading) }
    )

}

@Composable
private fun ColumnScope.KanjiDetails(
    details: LetterPracticeItemData.KanjiWritingData,
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

    KanjiReadingsContainer(
        on = details.on,
        kun = details.kun,
        modifier = Modifier.fillMaxWidth()
    )

    if (details.variants != null) {

        KanjiVariantsRow(details.variants)

        val unicodeHex = String.format("U+%04X", details.character.first().code)
        Text(text = resolveString { letterPractice.unicodeTitle(unicodeHex) })

        Text(text = resolveString { letterPractice.strokeCountTitle(details.strokes.size) })

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
                strokes = getColoredKanjiStrokes(
                    strokes = strokes,
                    radicalToStrokeRangeList = radicals.map {
                        val radicalStrokeRange =
                            it.startPosition until (it.startPosition + it.strokesCount)
                        it.radical to radicalStrokeRange
                    }
                ),
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

    if (meanings.isNotEmpty()) {
        Text(
            text = meanings.joinToString(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = modifier
        )
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
            text = resolveString { letterPractice.headerWordsMessage(words.size) },
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
                        // TODO hidden kanji, romaji
                        FuriganaText(
                            furiganaString = it.reading.run {
                                furigana ?: kanjiReading?.toFurigana() ?: kanaReading.toFurigana()
                            },
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                } else {
                    FuriganaText(
                        furiganaString = words.first().reading.run {
                            furigana ?: kanjiReading?.toFurigana() ?: kanaReading.toFurigana()
                        },
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
