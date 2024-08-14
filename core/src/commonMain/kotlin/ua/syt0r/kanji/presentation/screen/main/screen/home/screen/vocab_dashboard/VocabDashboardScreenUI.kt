package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.snapSizeTransform
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardEmptyState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListItemContainer
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListItemDetails
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListItemHeader
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListMode
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardLoadedStateContainer
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckStudyType
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksMergeRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksSortRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.VocabDeckDashboardItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.addMergeItems
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.addSortItems
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.deckDashboardListModeButtons
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.ui.VocabDashboardBottomBarUI


@Composable
fun VocabDashboardScreenUI(
    screenState: State<ScreenState>,
    mergeDecks: (DecksMergeRequestData) -> Unit,
    sortDecks: (DecksSortRequestData) -> Unit,
    navigateToDeckDetails: (VocabDeckDashboardItem) -> Unit,
    startQuickPractice: (VocabDeckDashboardItem, DeckStudyType, List<Long>) -> Unit,
    createDeck: () -> Unit
) {

    val extraListSpacerState = rememberExtraListSpacerState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        AnimatedContent(
            targetState = screenState.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() using snapSizeTransform() },
            modifier = Modifier.fillMaxSize()
        ) { screenState ->

            when (screenState) {
                ScreenState.Loading -> FancyLoading(Modifier.fillMaxSize().wrapContentSize())
                is ScreenState.Loaded -> {
                    if (screenState.listState.items.isEmpty()) {
                        DeckDashboardEmptyState()
                    } else {
                        DeckDashboardLoadedStateContainer(extraListSpacerState) {

                            if (screenState.listState.items.size > 1) {
                                deckDashboardListModeButtons(
                                    listState = screenState.listState,
                                    mergeDecks = mergeDecks,
                                    sortDecks = sortDecks
                                )
                            }

                            when (val currentMode = screenState.listState.mode.value) {
                                is DeckDashboardListMode.Browsing -> {
                                    screenState.listState.addBrowseItems(
                                        scope = this,
                                        studyType = screenState.srsPracticeType,
                                        navigateToDetails = navigateToDeckDetails,
                                        navigateToPractice = startQuickPractice
                                    )
                                }

                                is DeckDashboardListMode.MergeMode -> {
                                    screenState.listState.addMergeItems(
                                        scope = this,
                                        listMode = currentMode
                                    )
                                }

                                is DeckDashboardListMode.SortMode -> {
                                    screenState.listState.addSortItems(
                                        scope = this,
                                        listMode = currentMode
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }

        VocabDashboardBottomBarUI(
            state = screenState,
            navigateToDeckPicker = createDeck,
            modifier = Modifier.align(Alignment.BottomCenter)
                .onGloballyPositioned { extraListSpacerState.updateOverlay(it) }
        )

    }

}


private fun DeckDashboardListState.addBrowseItems(
    scope: LazyListScope,
    studyType: State<DeckStudyType>,
    navigateToDetails: (VocabDeckDashboardItem) -> Unit,
    navigateToPractice: (VocabDeckDashboardItem, DeckStudyType, List<Long>) -> Unit,
) = scope.apply {

    items(
        items = items,
        key = { DeckDashboardListMode.Browsing::class.simpleName to it.id }
    ) {

        VocabDeckItem(
            studyType = studyType,
            item = it as VocabDeckDashboardItem,
            navigateToDetails = { navigateToDetails(it) },
            navigateToPractice = navigateToPractice,
        )

    }

}


@Composable
private fun VocabDeckItem(
    studyType: State<DeckStudyType>,
    item: VocabDeckDashboardItem,
    navigateToDetails: () -> Unit,
    navigateToPractice: (VocabDeckDashboardItem, DeckStudyType, List<Long>) -> Unit
) {
    val studyProgress = remember {
        derivedStateOf { item.studyProgress.getValue(studyType.value) }
    }

    DeckDashboardListItemContainer(
        itemKey = item.id,
        header = {

            DeckDashboardListItemHeader(
                title = item.title,
                elapsedSinceLastReview = item.elapsedSinceLastReview,
                onDetailsClick = navigateToDetails
            ) {
                if (studyProgress.value.quickReview.isNotEmpty())
                    Box(
                        modifier = Modifier.clip(CircleShape).size(6.dp)
                            .background(MaterialTheme.extraColorScheme.due)
                    )
            }
        },
        details = {
            DeckDashboardListItemDetails(
                studyProgress = studyProgress.value,
                indicatorColumnTopContent = {},
                indicatorsRowContentAlignment = Alignment.CenterVertically,
                navigateToPractice = { navigateToPractice(item, studyType.value, it) }
            )
        }
    )
}
