package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.CustomRippleTheme
import ua.syt0r.kanji.presentation.common.ui.MultiplatformPopup
import ua.syt0r.kanji.presentation.common.ui.PreferredPopupLocation
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.LetterDeckDetailsCharacterBox
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.LetterDeckDetailsContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.CharacterReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsListItem
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsVisibleData
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.PracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.toColor

private sealed interface SheetContentState {

    object Loading : SheetContentState

    object NothingSelected : SheetContentState

    data class Loaded(
        val practiceType: PracticeType,
        val group: DeckDetailsListItem.Group,
    ) : SheetContentState

}

@Composable
private fun State<ScreenState>.toSheetContentState(): State<SheetContentState> {
    return remember {
        derivedStateOf {
            val currentState = value
                .let { it as? ScreenState.Loaded }
                ?: return@derivedStateOf SheetContentState.Loading

            SheetContentState.Loaded(
                practiceType = currentState.visibleDataState.value.configuration.practiceType,
                group = currentState.visibleDataState.value
                    .let { it as? DeckDetailsVisibleData.Groups }
                    ?.selectedItem
                    ?.value
                    ?: return@derivedStateOf SheetContentState.NothingSelected
            )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterDeckDetailsBottomSheet(
    state: State<ScreenState>,
    onCharacterClick: (String) -> Unit,
    onStudyClick: (DeckDetailsListItem.Group) -> Unit,
    onDismissRequest: suspend () -> Unit,
) {

    val sheetContentState = state.toSheetContentState()
    SheetVisibilityAutoToggleLaunchedEffect(sheetContentState, onDismissRequest)

    Column(
        modifier = Modifier.animateContentSize(tween(100, easing = LinearEasing))
    ) {

        BottomSheetDefaults.DragHandle(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        when (val currentState = sheetContentState.value) {
            SheetContentState.Loading,
            SheetContentState.NothingSelected -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .wrapContentSize()
                )
            }

            is SheetContentState.Loaded -> {
                PracticeGroupDetails(
                    practiceType = currentState.practiceType,
                    group = currentState.group,
                    onCharacterClick = onCharacterClick,
                    onStartClick = { onStudyClick(currentState.group) }
                )
            }
        }
    }

}


@Composable
private fun PracticeGroupDetails(
    practiceType: PracticeType,
    group: DeckDetailsListItem.Group,
    onCharacterClick: (String) -> Unit = {},
    onStartClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = resolveString { letterDeckDetails.detailsGroupTitle(group.index) },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 1.dp) // TODO text alignment api
            )

            var hintDropdownShown by remember { mutableStateOf(false) }

            val dotColor = group.reviewState.toColor()
            val rippleTheme = remember(dotColor) { CustomRippleTheme { dotColor } }

            CompositionLocalProvider(LocalRippleTheme provides rippleTheme) {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clip(CircleShape)
                        .clickable { hintDropdownShown = true }
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .align(Alignment.Center)
                    )

                    MultiplatformPopup(
                        expanded = hintDropdownShown,
                        onDismissRequest = { hintDropdownShown = false },
                        preferredPopupLocation = PreferredPopupLocation.Top
                    ) {
                        Text(
                            text = resolveString {
                                when (group.reviewState) {
                                    CharacterReviewState.Done -> {
                                        reviewStateDone
                                    }

                                    CharacterReviewState.Due -> {
                                        reviewStateDue
                                    }

                                    CharacterReviewState.New -> {
                                        reviewStateNew
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

        }

        Text(
            text = resolveString {
                letterDeckDetails.firstTimeReviewMessage(group.summary.firstReviewDate)
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Text(
            text = resolveString {
                letterDeckDetails.lastTimeReviewMessage(group.summary.lastReviewDate)
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth().height(65.dp),
        ) {

            item { Spacer(Modifier.width(20.dp)) }

            items(group.items) {

                val reviewState = when (practiceType) {
                    PracticeType.Writing -> it.writingSummary.state
                    PracticeType.Reading -> it.readingSummary.state
                }

                LetterDeckDetailsCharacterBox(
                    character = it.character,
                    reviewState = reviewState,
                    onClick = { onCharacterClick(it.character) },
                    modifier = Modifier.padding(end = 12.dp)
                )

            }

            item { Spacer(Modifier.width(8.dp)) }

        }

        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            FilledTonalButton(
                onClick = onStartClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = resolveString { letterDeckDetails.groupDetailsButton })
            }

        }

    }

}

@Composable
private fun SheetVisibilityAutoToggleLaunchedEffect(
    state: State<SheetContentState>,
    onDismissRequest: suspend () -> Unit,
) {
    LaunchedEffect(Unit) {
        snapshotFlow { state.value }
            .filterIsInstance<SheetContentState.NothingSelected>()
            .collectLatest { onDismissRequest() }
    }
}
