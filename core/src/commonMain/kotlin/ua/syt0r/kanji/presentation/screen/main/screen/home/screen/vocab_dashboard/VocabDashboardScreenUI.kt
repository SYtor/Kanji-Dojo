package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import ua.syt0r.kanji.presentation.common.ExtraListSpacerState
import ua.syt0r.kanji.presentation.common.ExtraSpacer
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.BottomSheetState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.ui.VocabDashboardBottomSheet


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabDashboardScreenUI(
    screenState: State<ScreenState>,
    bottomSheetState: State<BottomSheetState>,
    select: (DashboardVocabDeck) -> Unit,
    createDeck: () -> Unit,
    onEditClick: (DashboardVocabDeck) -> Unit,
    navigateToPractice: (MainDestination.VocabPractice) -> Unit
) {

    val extraListSpacerState = rememberExtraListSpacerState()

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            tonalElevation = 0.dp
        ) {
            VocabDashboardBottomSheet(
                state = bottomSheetState,
                onEditClick = { onEditClick(it) },
                navigateToPractice = navigateToPractice
            )
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { bottomSheetState.value }
            .filterIsInstance<BottomSheetState.Hidden>()
            .onEach {
                sheetState.hide()
                showBottomSheet = false
            }
            .collect()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AnimatedContent(
            targetState = screenState.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.fillMaxSize()
        ) { screenState ->

            when (screenState) {
                ScreenState.Loading -> FancyLoading(Modifier.fillMaxSize().wrapContentSize())
                is ScreenState.Loaded -> ScreenLoadedState(
                    screenState = screenState,
                    extraListSpacerState = extraListSpacerState,
                    select = {
                        select(it)
                        showBottomSheet = true
                    }
                )
            }

        }

        FloatingActionButton(
            onClick = createDeck,
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(20.dp)
                .onGloballyPositioned { extraListSpacerState.updateOverlay(it) }
        ) {
            Icon(Icons.Default.Add, null)
        }

    }

}

@Composable
private fun ScreenLoadedState(
    screenState: ScreenState.Loaded,
    extraListSpacerState: ExtraListSpacerState,
    select: (DashboardVocabDeck) -> Unit
) {

    val orientation = LocalOrientation.current

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp,
        modifier = Modifier.fillMaxSize()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp)
            .onGloballyPositioned { extraListSpacerState.updateList(it) }
    ) {

        if (orientation == Orientation.Landscape) {
            item(span = StaggeredGridItemSpan.FullLine) { Spacer(Modifier.height(20.dp)) }
        }

        item(span = StaggeredGridItemSpan.FullLine) {
            Text(
                text = resolveString { vocabDashboard.userDecksTitle },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        val srsPracticeType = screenState.srsPracticeType.value

        if (screenState.userDecks.isNotEmpty()) {
            items(screenState.userDecks) {
                PracticeGridItem(
                    title = resolveString(it.titleResolver),
                    hasDue = it.srsProgress.getValue(srsPracticeType).due.isNotEmpty(),
                    onClick = { select(it) }
                )
            }
        } else {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(resolveString { vocabDashboard.userDecksEmptyMessage })
            }
        }

        item(span = StaggeredGridItemSpan.FullLine) { Divider(Modifier.fillMaxWidth()) }

        item(span = StaggeredGridItemSpan.FullLine) {
            Text(
                text = resolveString { vocabDashboard.defaultDecksTitle },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(screenState.defaultDecks) { vocabPracticeSet ->
            PracticeGridItem(
                title = resolveString(vocabPracticeSet.titleResolver),
                hasDue = vocabPracticeSet.srsProgress.getValue(srsPracticeType).due.isNotEmpty(),
                onClick = { select(vocabPracticeSet) }
            )
        }

        extraListSpacerState.ExtraSpacer(this)

    }

}

@Composable
private fun PracticeGridItem(
    title: String,
    hasDue: Boolean,
    onClick: () -> Unit
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).alignByBaseline()
        )
        if (hasDue) {
            Box(
                modifier = Modifier.alignBy { it.measuredHeight }
                    .size(10.dp)
                    .background(MaterialTheme.extraColorScheme.due, CircleShape)
            )
        }
    }
}
