package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReadMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.AutopaddedScrollableColumn
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.neutralTextButtonColors
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterDecorations
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWritingProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.ExpandablePracticeAnswerButtonsRow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.ExpandableVocabPracticeAnswersRowState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabCharacterWritingData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewState

@Composable
fun VocabPracticeWritingUI(
    reviewState: VocabReviewState.Writing,
    answers: PracticeAnswers,
    onNextClick: (PracticeAnswer) -> Unit,
    onWordClick: (JapaneseWord) -> Unit,
    onFeedbackClick: (JapaneseWord) -> Unit
) {

    AutoSwitchSelectedItemLaunchedEffect(reviewState)

    val revealAnswer = remember(reviewState) {
        derivedStateOf {
            reviewState.charactersData.filterIsInstance<VocabCharacterWritingData.WithStrokes>()
                .all { it.writerState.progress.value is CharacterWritingProgress.Completed }
        }
    }

    val showAnswerButtons = remember(reviewState) {
        derivedStateOf {
            reviewState.charactersData.filterIsInstance<VocabCharacterWritingData.WithStrokes>()
                .all { it.writerState.progress.value is CharacterWritingProgress.Completed.Idle }
        }
    }

    val updatedState = rememberUpdatedState(showAnswerButtons to answers)

    val answersRowState = remember {
        derivedStateOf {
            val (updatedShowNextButtonState, updatedAnswers) = updatedState.value
            ExpandableVocabPracticeAnswersRowState(
                answers = updatedAnswers,
                showButton = updatedShowNextButtonState.value
            )
        }
    }

    val selectedReviewState = rememberUpdatedState(reviewState.selected.value)

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when (LocalOrientation.current) {
            Orientation.Portrait -> {
                AutopaddedScrollableColumn(
                    modifier = Modifier.fillMaxSize(),
                    bottomOverlayContent = {
                        Input(
                            state = selectedReviewState,
                            answersState = answersRowState,
                            onNextClick = onNextClick,
                            modifier = Modifier.fillMaxWidth()
                                .padding(20.dp)
                                .wrapContentSize()
                                .widthIn(max = 400.dp)
                                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                        )
                    }
                ) {

                    Progress(
                        reviewState = reviewState,
                        onWordClick = onWordClick,
                        revealAnswer = revealAnswer,
                        modifier = Modifier.fillMaxSize().padding(20.dp)
                    )

                }
            }

            Orientation.Landscape -> {
                Row {

                    Progress(
                        reviewState = reviewState,
                        onWordClick = onWordClick,
                        revealAnswer = revealAnswer,
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    )

                    Input(
                        state = selectedReviewState,
                        answersState = answersRowState,
                        onNextClick = onNextClick,
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight()
                            .padding(20.dp)
                            .wrapContentSize()
                            .widthIn(max = 400.dp)
                            .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    )

                }
            }
        }

    }

}

@Composable
private fun Progress(
    reviewState: VocabReviewState.Writing,
    revealAnswer: State<Boolean>,
    onWordClick: (JapaneseWord) -> Unit,
    modifier: Modifier,
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = reviewState.word.meanings.first(),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        val lazyListState = rememberLazyListState()
        AutoscrollCharacterIndicatorRowLaunchedEffect(reviewState, lazyListState)

        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            state = lazyListState
        ) {

            items(reviewState.charactersData) {
                CharacterStateIndicator(
                    characterData = it,
                    selectedState = reviewState.selected
                )
            }

        }

        val detailsAlpha = if (revealAnswer.value) 1f else 0f

        TextButton(
            onClick = { onWordClick(reviewState.word) },
            enabled = detailsAlpha != 0f,
            modifier = Modifier.graphicsLayer { alpha = detailsAlpha },
            colors = ButtonDefaults.neutralTextButtonColors()
        ) {
            Text(
                text = resolveString { vocabPractice.detailsButton }
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReadMore,
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

    }

}

@Composable
private fun Input(
    state: State<VocabCharacterWritingData>,
    answersState: State<ExpandableVocabPracticeAnswersRowState>,
    onNextClick: (PracticeAnswer) -> Unit,
    modifier: Modifier
) {

    CharacterWriterDecorations(
        modifier = modifier,
        state = remember {
            derivedStateOf {
                state.value.let { it as? VocabCharacterWritingData.WithStrokes }?.writerState
            }
        }
    ) {

        Crossfade(
            targetState = state.value
        ) {

            when (it) {
                is VocabCharacterWritingData.NoStrokes -> {}
                is VocabCharacterWritingData.WithStrokes -> CharacterWriter(
                    state = it.writerState,
                    modifier = Modifier.fillMaxSize()
                )
            }

        }

        ExpandablePracticeAnswerButtonsRow(
            state = answersState,
            onClick = onNextClick,
            modifier = Modifier.fillMaxWidth()
                .height(IntrinsicSize.Max)
                .align(Alignment.BottomCenter)
        )

    }

}

private enum class CharacterWritingDisplayState { NoWritingData, Writing, Correct, Failed }

@Composable
private fun CharacterStateIndicator(
    characterData: VocabCharacterWritingData,
    selectedState: MutableState<VocabCharacterWritingData>
) {

    val state = when (characterData) {
        is VocabCharacterWritingData.NoStrokes -> {
            CharacterWritingDisplayState.NoWritingData
        }

        is VocabCharacterWritingData.WithStrokes -> {
            when (val status = characterData.writerState.progress.value) {
                CharacterWritingProgress.Writing -> {
                    CharacterWritingDisplayState.Writing
                }

                is CharacterWritingProgress.Completed -> {
                    if (status.isCorrect) CharacterWritingDisplayState.Correct
                    else CharacterWritingDisplayState.Failed
                }

            }
        }
    }

    val borderColor = when (state) {
        CharacterWritingDisplayState.NoWritingData,
        CharacterWritingDisplayState.Writing -> MaterialTheme.colorScheme.surfaceVariant

        CharacterWritingDisplayState.Failed -> MaterialTheme.colorScheme.error
        CharacterWritingDisplayState.Correct -> MaterialTheme.extraColorScheme.success
    }

    val textColor = when (state) {
        CharacterWritingDisplayState.NoWritingData -> MaterialTheme.colorScheme
            .onSurface.copy(alpha = 0.6f)

        CharacterWritingDisplayState.Writing -> MaterialTheme.colorScheme.surfaceVariant
        CharacterWritingDisplayState.Correct,
        CharacterWritingDisplayState.Failed -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        Text(
            text = characterData.character,
            color = textColor,
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable(
                    enabled = state != CharacterWritingDisplayState.NoWritingData
                ) { selectedState.value = characterData }
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, borderColor, MaterialTheme.shapes.small)
                .wrapContentSize()
        )

        Box(
            Modifier.fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 4.dp)
                .clip(CircleShape)
                .background(
                    when (characterData == selectedState.value) {
                        true -> MaterialTheme.colorScheme.onSurface
                        false -> MaterialTheme.colorScheme.surface
                    }
                )
        )

    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun AutoSwitchSelectedItemLaunchedEffect(reviewState: VocabReviewState.Writing) {
    LaunchedEffect(reviewState) {
        val completedItemIndexes = mutableSetOf<Int>()
        val selectedStateToIndexFlow = snapshotFlow {
            val selected = reviewState.selected.value
            val index = reviewState.charactersData.indexOf(selected)
            selected to index
        }

        selectedStateToIndexFlow.filter { (state, index) -> !completedItemIndexes.contains(index) }
            // Filters selected data with completed statuses only
            .flatMapLatest { (state: VocabCharacterWritingData, index: Int) ->
                if (state !is VocabCharacterWritingData.WithStrokes)
                    return@flatMapLatest flow<Int> { }

                val writingStatusFlow = snapshotFlow { state.writerState.progress.value }
                writingStatusFlow.filterIsInstance<CharacterWritingProgress.Completed>()
                    .map { index }
            }
            .onEach { index ->
                completedItemIndexes.add(index)
                delay(800)
                val nextState = reviewState.charactersData
                    .filterIsInstance<VocabCharacterWritingData.WithStrokes>()
                    .find { it.writerState.progress.value !is CharacterWritingProgress.Completed }
                    ?: return@onEach
                reviewState.selected.value = nextState
            }
            .collect()
    }
}

@Composable
fun AutoscrollCharacterIndicatorRowLaunchedEffect(
    reviewState: VocabReviewState.Writing,
    lazyListState: LazyListState
) {
    LaunchedEffect(reviewState) {
        snapshotFlow { reviewState.selected.value }
            .collectLatest {
                val index = reviewState.charactersData.indexOf(it)

                val offset = lazyListState.layoutInfo.visibleItemsInfo.first().size * 3
                val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
                if (index < firstVisibleItemIndex + 3)
                    lazyListState.animateScrollToItem(index, -offset)

                val lastVisibleItemIndex = lazyListState.layoutInfo.visibleItemsInfo.last().index
                if (index > lastVisibleItemIndex - 3)
                    lazyListState.animateScrollToItem(index, offset)
            }
    }
}
