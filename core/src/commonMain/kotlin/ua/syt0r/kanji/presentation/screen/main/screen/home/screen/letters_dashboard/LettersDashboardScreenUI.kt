package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

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
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
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
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckDashboardItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.addMergeItems
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.addSortItems
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.deckDashboardListModeButtons
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.ui.LetterDashboardBottomBarUI

@Composable
fun LettersDashboardScreenUI(
    state: State<ScreenState>,
    mergeDecks: (DecksMergeRequestData) -> Unit,
    sortDecks: (DecksSortRequestData) -> Unit,
    navigateToDeckDetails: (LetterDeckDashboardItem) -> Unit,
    startQuickPractice: (LetterDeckDashboardItem, ScreenLetterPracticeType, List<String>) -> Unit,
    navigateToDeckPicker: () -> Unit
) {

    Box {

        val extraListSpacerState = rememberExtraListSpacerState()

        AnimatedContent(
            targetState = state.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() using snapSizeTransform() },
        ) { screenState ->
            when (screenState) {
                ScreenState.Loading -> {
                    LoadingState()
                }

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
                                        showPendingNewIndicator = screenState.listState.showDailyNewIndicator,
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

        LetterDashboardBottomBarUI(
            state = state,
            navigateToDeckPicker = navigateToDeckPicker,
            modifier = Modifier.align(Alignment.BottomCenter)
                .onGloballyPositioned { extraListSpacerState.updateOverlay(it) },
        )

    }

}

@Composable
private fun LoadingState() {
    FancyLoading(Modifier.fillMaxSize().wrapContentSize())
}

private fun DeckDashboardListState.addBrowseItems(
    scope: LazyListScope,
    practiceType: State<ScreenLetterPracticeType>,
    showPendingNewIndicator: Boolean,
    navigateToDetails: (LetterDeckDashboardItem) -> Unit,
    navigateToPractice: (LetterDeckDashboardItem, ScreenLetterPracticeType, List<String>) -> Unit,
) = scope.apply {

    items(
        items = items,
        key = { DeckDashboardListMode.Browsing::class.simpleName to it.deckId }
    ) {

        LetterDeckItem(
            item = it as LetterDeckDashboardItem,
            practiceType = practiceType,
            showPendingNewIndicator = showPendingNewIndicator,
            navigateToDetails = { navigateToDetails(it) },
            navigateToPractice = navigateToPractice,
        )

    }

}

@Composable
private fun LetterDeckItem(
    item: LetterDeckDashboardItem,
    practiceType: State<ScreenLetterPracticeType>,
    showPendingNewIndicator: Boolean,
    navigateToDetails: () -> Unit,
    navigateToPractice: (LetterDeckDashboardItem, ScreenLetterPracticeType, List<String>) -> Unit
) {

    val studyProgress = remember {
        derivedStateOf { item.studyProgress.getValue(practiceType.value) }
    }

    DeckDashboardListItem(
        itemKey = item.deckId,
        title = item.title,
        elapsedSinceLastReview = item.elapsedSinceLastReview,
        showNewIndicator = showPendingNewIndicator,
        studyProgress = studyProgress.value,
        onDetailsClick = navigateToDetails,
        navigateToPractice = { navigateToPractice(item, practiceType.value, it) }
    )

}
