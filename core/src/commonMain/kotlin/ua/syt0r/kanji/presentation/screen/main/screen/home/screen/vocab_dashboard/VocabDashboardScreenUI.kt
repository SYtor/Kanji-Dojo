package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.theme.snapSizeTransform
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardEmptyState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListMode
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardListState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckDashboardLoadedStateContainer
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
    startQuickPractice: (VocabDeckDashboardItem, ScreenVocabPracticeType, List<Long>) -> Unit,
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
                        val practiceType = remember {
                            derivedStateOf { screenState.selectedPracticeTypeItem.value.practiceType }
                        }
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
                                        practiceType = practiceType,
                                        showNewIndicator = screenState.listState.showDailyNewIndicator,
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
    practiceType: State<ScreenVocabPracticeType>,
    showNewIndicator: Boolean,
    navigateToDetails: (VocabDeckDashboardItem) -> Unit,
    navigateToPractice: (VocabDeckDashboardItem, ScreenVocabPracticeType, List<Long>) -> Unit,
) = scope.apply {

    items(
        items = items,
        key = { DeckDashboardListMode.Browsing::class.simpleName to it.deckId }
    ) {

        VocabDeckItem(
            practiceType = practiceType,
            item = it as VocabDeckDashboardItem,
            showNewIndicator = showNewIndicator,
            navigateToDetails = { navigateToDetails(it) },
            navigateToPractice = navigateToPractice,
        )

    }

}


@Composable
private fun VocabDeckItem(
    practiceType: State<ScreenVocabPracticeType>,
    item: VocabDeckDashboardItem,
    showNewIndicator: Boolean,
    navigateToDetails: () -> Unit,
    navigateToPractice: (VocabDeckDashboardItem, ScreenVocabPracticeType, List<Long>) -> Unit
) {

    val studyProgress = remember {
        derivedStateOf { item.studyProgress.getValue(practiceType.value) }
    }

    DeckDashboardListItem(
        itemKey = item.deckId,
        title = item.title,
        elapsedSinceLastReview = item.elapsedSinceLastReview,
        showNewIndicator = showNewIndicator,
        studyProgress = studyProgress.value,
        onDetailsClick = navigateToDetails,
        navigateToPractice = { navigateToPractice(item, practiceType.value, it) }
    )

}
