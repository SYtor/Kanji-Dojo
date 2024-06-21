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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.AutopaddedScrollableColumn
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.neutralTextButtonColors
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterDecorations
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWriterState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWritingStatus
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeNextButton
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewState

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun VocabPracticeWritingUI(
    reviewState: VocabReviewState.Writing,
    onNextClick: () -> Unit,
    onWordClick: (JapaneseWord) -> Unit,
    onFeedbackClick: (JapaneseWord) -> Unit
) {

    val showNextButton = remember(reviewState) {
        derivedStateOf {
            reviewState.charactersData
                .all { it.writingStatus.value is CharacterWritingStatus.Completed }
        }
    }

    LaunchedEffect(reviewState) {
        val completedStateIndex = mutableSetOf<Int>()
        snapshotFlow {
            val selected = reviewState.selected.value
            val index = reviewState.charactersData.indexOf(selected)
            selected to index
        }
            .filter { (state, index) -> !completedStateIndex.contains(index) }
            .flatMapLatest { (state, index) ->
                snapshotFlow { Triple(state, index, state.writingStatus.value) }
            }
            .filter { (state, index, status) -> status is CharacterWritingStatus.Completed }
            .onEach { (state, index, status) ->
                completedStateIndex.add(index)
                delay(800)
                val nextState = reviewState.charactersData
                    .find { it.writingStatus.value !is CharacterWritingStatus.Completed }
                    ?: return@onEach
                reviewState.selected.value = nextState
            }
            .collect()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when (LocalOrientation.current) {
            Orientation.Portrait -> {
                AutopaddedScrollableColumn(
                    modifier = Modifier.fillMaxSize(),
                    bottomOverlayContent = {
                        Input(
                            state = reviewState.selected,
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
                        isNextButtonShown = showNextButton,
                        modifier = Modifier.fillMaxSize().padding(20.dp)
                    )

                }
            }

            Orientation.Landscape -> {
                Row {

                    Progress(
                        reviewState = reviewState,
                        onWordClick = onWordClick,
                        isNextButtonShown = showNextButton,
                        modifier = Modifier.weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    )

                    Input(
                        state = reviewState.selected,
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

        VocabPracticeNextButton(
            showNextButton = showNextButton,
            onClick = onNextClick,
            onFeedbackClick = { onFeedbackClick(reviewState.word) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

    }

}

@Composable
private fun Progress(
    reviewState: VocabReviewState.Writing,
    isNextButtonShown: State<Boolean>,
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
            style = MaterialTheme.typography.displaySmall
        )

        val scrollState = rememberLazyListState()
        LaunchedEffect(reviewState) {
            snapshotFlow { reviewState.selected.value }
                .collectLatest {
                    val index = reviewState.charactersData.indexOf(it)

                    val offset = scrollState.layoutInfo.visibleItemsInfo.first().size * 3
                    val firstVisibleItemIndex = scrollState.firstVisibleItemIndex
                    if (index < firstVisibleItemIndex + 3)
                        scrollState.animateScrollToItem(index, -offset)

                    val lastVisibleItemIndex = scrollState.layoutInfo.visibleItemsInfo.last().index
                    if (index > lastVisibleItemIndex - 3)
                        scrollState.animateScrollToItem(index, offset)
                }
        }

        LazyRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            state = scrollState
        ) {

            items(reviewState.charactersData) {
                CharacterStateIndicator(
                    writerState = it,
                    selectedState = reviewState.selected
                )
            }

        }

        val detailsAlpha = if (isNextButtonShown.value) 1f else 0f

        TextButton(
            onClick = { onWordClick(reviewState.word) },
            enabled = detailsAlpha != 0f,
            modifier = Modifier.graphicsLayer { alpha = detailsAlpha },
            colors = ButtonDefaults.neutralTextButtonColors()
        ) {
            Text("Details")
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
    state: State<CharacterWriterState>,
    modifier: Modifier
) {
    CharacterWriterDecorations(
        modifier = modifier
    ) {

        Crossfade(
            targetState = state.value
        ) {

            CharacterWriter(
                state = it,
                modifier = Modifier.fillMaxSize()
            )

        }
    }
}

@Composable
private fun CharacterStateIndicator(
    writerState: CharacterWriterState,
    selectedState: MutableState<CharacterWriterState>
) {

    val status = writerState.writingStatus.value

    val borderColor = when {
        status !is CharacterWritingStatus.Completed -> MaterialTheme.colorScheme.surfaceVariant
        status.mistakes > 2 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.extraColorScheme.success
    }

    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {

        Text(
            text = writerState.character,
            color = when (status) {
                CharacterWritingStatus.InProcess -> MaterialTheme.colorScheme.surfaceVariant
                is CharacterWritingStatus.Completed -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { selectedState.value = writerState }
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(2.dp, borderColor, MaterialTheme.shapes.small)
                .wrapContentSize()
        )

        Box(
            Modifier.fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 2.dp)
                .clip(CircleShape)
                .background(
                    when (writerState == selectedState.value) {
                        true -> MaterialTheme.colorScheme.onSurface
                        false -> MaterialTheme.colorScheme.surface
                    }
                )
        )

    }
}