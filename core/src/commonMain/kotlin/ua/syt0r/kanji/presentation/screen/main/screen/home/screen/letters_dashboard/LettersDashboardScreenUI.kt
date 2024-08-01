package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.srs.DailyLimitConfiguration
import ua.syt0r.kanji.presentation.common.rememberExtraListSpacerState
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
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
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckDashboardItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckStudyType
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
    startQuickPractice: (LetterDeckDashboardItem, DeckStudyType, List<String>) -> Unit,
    updateDailyLimit: (DailyLimitConfiguration) -> Unit,
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
                                        dailyGoalEnabled = screenState.dailyIndicatorData.configuration.enabled,
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
            updateConfiguration = updateDailyLimit,
            modifier = Modifier.align(Alignment.BottomCenter)
                .onGloballyPositioned { extraListSpacerState.updateOverlay(it) },
        )

    }

}

@Composable
private fun LoadingState() {
    FancyLoading(Modifier.fillMaxSize().wrapContentSize())
}

fun DeckDashboardListState.addBrowseItems(
    scope: LazyListScope,
    dailyGoalEnabled: Boolean,
    navigateToDetails: (LetterDeckDashboardItem) -> Unit,
    navigateToPractice: (LetterDeckDashboardItem, DeckStudyType, List<String>) -> Unit,
) = scope.apply {

    items(
        items = items,
        key = { DeckDashboardListMode.Browsing::class.simpleName to it.id }
    ) {

        LetterDeckItem(
            item = it as LetterDeckDashboardItem,
            dailyGoalEnabled = dailyGoalEnabled,
            navigateToDetails = { navigateToDetails(it) },
            navigateToPractice = navigateToPractice,
        )

    }

}

@Composable
private fun LetterDeckItem(
    item: LetterDeckDashboardItem,
    dailyGoalEnabled: Boolean,
    navigateToDetails: () -> Unit,
    navigateToPractice: (LetterDeckDashboardItem, DeckStudyType, List<String>) -> Unit
) {

    val studyType: MutableState<DeckStudyType> = remember {
        mutableStateOf(LetterDeckStudyType.Writing)
    }

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

                Column(
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    val writingProgress = item.studyProgress.getValue(LetterDeckStudyType.Writing)
                    DeckPendingReviewsCountIndicator(
                        icon = Icons.Default.Draw,
                        dailyGoalEnabled = dailyGoalEnabled,
                        study = writingProgress.quickLearn.size,
                        review = writingProgress.quickReview.size
                    )

                    val readingProgress = item.studyProgress.getValue(LetterDeckStudyType.Reading)
                    DeckPendingReviewsCountIndicator(
                        icon = Icons.Default.LocalLibrary,
                        dailyGoalEnabled = dailyGoalEnabled,
                        study = readingProgress.quickLearn.size,
                        review = readingProgress.quickReview.size
                    )
                }

            }
        },
        details = {
            DeckDashboardListItemDetails(
                studyProgress = studyProgress.value,
                extraIndicatorContent = { PracticeTypeSwitch(studyType) },
                navigateToPractice = { navigateToPractice(item, studyType.value, it) }
            )
        }
    )
}

@Composable
private fun DeckPendingReviewsCountIndicator(
    icon: ImageVector,
    dailyGoalEnabled: Boolean,
    study: Int,
    review: Int
) {
    val showStudy = study > 0 && dailyGoalEnabled
    val showDue = review > 0
    if (!showStudy && !showDue) return

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
        if (showStudy) {
            Box(
                modifier = Modifier.align(Alignment.CenterVertically).size(4.dp)
                    .background(MaterialTheme.extraColorScheme.new, CircleShape)
            )
        }
        if (showDue) {
            Box(
                modifier = Modifier.align(Alignment.CenterVertically).size(4.dp)
                    .background(MaterialTheme.extraColorScheme.due, CircleShape)
            )
        }
    }
}

@Composable
private fun ColumnScope.PracticeTypeSwitch(
    studyType: MutableState<DeckStudyType>
) {

    val switchEnabled: Boolean
    val icon: ImageVector
    val title: String

    when (studyType.value) {
        LetterDeckStudyType.Writing -> {
            switchEnabled = false
            icon = Icons.Default.Draw
            title = resolveString { lettersDashboard.itemWritingTitle }
        }

        LetterDeckStudyType.Reading -> {
            switchEnabled = true
            icon = Icons.Default.LocalLibrary
            title = resolveString { lettersDashboard.itemReadingTitle }
        }

        else -> throw IllegalStateException()
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.align(Alignment.Start)
    ) {

        Switch(
            checked = switchEnabled,
            onCheckedChange = {
                if (it) studyType.value = LetterDeckStudyType.Reading
                else studyType.value = LetterDeckStudyType.Writing
            },
            thumbContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.outline,
                checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                checkedIconColor = MaterialTheme.colorScheme.surfaceVariant,
                checkedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedIconColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraLight
        )

    }

}
