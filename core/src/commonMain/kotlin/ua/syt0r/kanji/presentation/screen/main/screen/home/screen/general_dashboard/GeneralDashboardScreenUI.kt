package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.Github
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.snapSizeTransform
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.common.ui.PopupContentItem
import ua.syt0r.kanji.presentation.screen.VersionChangeDialog
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DeckStudyType
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.LetterDeckStudyType
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.ui.TutorialDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType

@Composable
fun GeneralDashboardScreenUI(
    state: State<ScreenState>,
    navigateToDailyLimitConfiguration: () -> Unit,
    navigateToCreateLetterDeck: () -> Unit,
    navigateToCreateVocabDeck: () -> Unit,
    navigateToLetterPractice: (MainDestination.Practice) -> Unit,
    navigateToVocabPractice: (MainDestination.VocabPractice) -> Unit,
    navigateToGithub: () -> Unit
) {

    var showTutorialDialog by remember { mutableStateOf(false) }
    if (showTutorialDialog) {
        TutorialDialog { showTutorialDialog = false }
    }

    var showVersionChangeDialog by remember { mutableStateOf(false) }
    if (showVersionChangeDialog) {
        VersionChangeDialog { showVersionChangeDialog = false }
    }

    ScreenLayout(
        state = state,
        header = {

            HeaderButton(
                onClick = navigateToDailyLimitConfiguration,
            ) {
                Text("Daily Limit")
                Icon(Icons.Outlined.Settings, null)
            }

            HeaderButton(
                onClick = {
                    showVersionChangeDialog = true
                    it.showAppVersionChangeHint.value = false
                }
            ) {
                if (it.showAppVersionChangeHint.value) {
                    Box(
                        modifier = Modifier.alignBy { it.measuredHeight }
                            .size(10.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }

                Text("What's new", Modifier.alignByBaseline())
                Icon(Icons.Outlined.Celebration, null)
            }

            HeaderButton(
                onClick = {
                    showTutorialDialog = true
                    it.showTutorialHint.value = false
                },
            ) {
                if (it.showTutorialHint.value) {
                    Box(
                        modifier = Modifier.alignBy { it.measuredHeight }
                            .size(10.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
                Text("Tutorial", Modifier.alignByBaseline())
                Icon(Icons.AutoMirrored.Outlined.HelpOutline, null)
            }

        },
        content = {

            val letterDecksTitle = resolveString { "Letter Decks" }

            when (it.letterDecksData) {
                is LetterDecksData.Data -> {
                    DashboardItemLayout(
                        title = letterDecksTitle,
                        studyTypeContent = {
                            StudyTypeSelector(
                                selectedType = it.letterDecksData.studyType,
                                availableStudyTypes = LetterDeckStudyType.values().toList(),
                            )
                        },
                        buttonsContent = {

                            val studyType = it.letterDecksData.studyType.value
                            val progress = it.letterDecksData.studyProgressMap.getValue(studyType)

                            val goToLetterPractice = { characters: Set<String> ->
                                val destination = when (studyType) {
                                    LetterDeckStudyType.Writing -> {
                                        MainDestination.Practice.Writing(-1, characters.toList())
                                    }

                                    LetterDeckStudyType.Reading -> {
                                        MainDestination.Practice.Reading(-1, characters.toList())
                                    }
                                }
                                navigateToLetterPractice(destination)
                            }

                            GeneralDashboardReviewButton(
                                onClick = { goToLetterPractice(progress.new) },
                                count = progress.new.size,
                                text = "New",
                                modifier = Modifier.weight(1f)
                            )

                            GeneralDashboardReviewButton(
                                onClick = { goToLetterPractice(progress.due) },
                                count = progress.due.size,
                                text = "Due",
                                modifier = Modifier.weight(1f)
                            )

                            GeneralDashboardReviewButton(
                                onClick = { goToLetterPractice(progress.combined) },
                                count = progress.combined.size,
                                text = "New & Due",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    )
                }

                LetterDecksData.NoDecks -> {
                    DashboardItemLayout(
                        title = letterDecksTitle,
                        buttonsContent = {
                            GeneralDashboardNoDecksButton(
                                onClick = navigateToCreateLetterDeck,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    )
                }

            }

            val vocabDecksTitle = resolveString { "Vocab Decks" }

            when (it.vocabDecksInfo) {
                is VocabDecksData.Data -> {

                    DashboardItemLayout(
                        title = vocabDecksTitle,
                        studyTypeContent = {
                            StudyTypeSelector(
                                selectedType = it.vocabDecksInfo.studyType,
                                availableStudyTypes = VocabPracticeType.values().toList(),
                            )
                        },
                        buttonsContent = {

                            val studyType = it.vocabDecksInfo.studyType.value
                            val progress = it.vocabDecksInfo.studyProgressMap.getValue(studyType)

                            val goToVocabPractice = { words: Set<Long> ->
                                val destination = MainDestination.VocabPractice(words.toList())
                                navigateToVocabPractice(destination)
                            }

                            GeneralDashboardReviewButton(
                                onClick = { goToVocabPractice(progress.due) },
                                count = progress.due.size,
                                text = "Due",
                                modifier = Modifier.weight(1f)
                            )

                            Box(Modifier.weight(2f))

                        }
                    )

                }

                VocabDecksData.NoDecks -> {

                    DashboardItemLayout(
                        title = vocabDecksTitle,
                        buttonsContent = {
                            GeneralDashboardNoDecksButton(
                                onClick = navigateToCreateVocabDeck,
                                modifier = Modifier
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = navigateToGithub) {
                    Icon(imageVector = ExtraIcons.Github, contentDescription = null)
                }
            }

        }
    )

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScreenLayout(
    state: State<ScreenState>,
    header: @Composable RowScope.(ScreenState.Loaded) -> Unit,
    content: @Composable ColumnScope.(ScreenState.Loaded) -> Unit
) {

    AnimatedContent(
        targetState = state.value,
        transitionSpec = { fadeIn() togetherWith fadeOut() using snapSizeTransform() }
    ) {

        when (it) {
            ScreenState.Loading -> FancyLoading(Modifier.fillMaxSize().wrapContentSize())
            is ScreenState.Loaded -> Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .wrapContentWidth()
            ) {

                if (LocalOrientation.current == Orientation.Landscape)
                    Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier.width(IntrinsicSize.Max)
                        .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 4.dp,
                            alignment = Alignment.CenterHorizontally
                        )
                    ) {
                        header(it)
                    }

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 20.dp)
                    )

                }

                content(it)

            }

        }

    }

}

@Composable
private fun HeaderButton(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.clip(ButtonDefaults.textShape)
            .clickable(onClick = onClick)
            .padding(ButtonDefaults.TextButtonContentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
private fun DashboardItemLayout(
    title: String,
    studyTypeContent: (@Composable () -> Unit)? = null,
    buttonsContent: @Composable RowScope.() -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .padding(bottom = 8.dp),
    ) {

        Row(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = resolveString { title },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (studyTypeContent != null) studyTypeContent.invoke()
        else Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            buttonsContent()

        }

    }
}

@Composable
private fun <T : DeckStudyType> StudyTypeSelector(
    selectedType: MutableState<T>,
    availableStudyTypes: List<T>,
) {

    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = resolveString { "Study Mode" },
            modifier = Modifier.alignByBaseline(),
        )

        var expanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier.alignByBaseline()
        ) {

            TextButton(
                onClick = { expanded = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                Text(
                    text = resolveString(selectedType.value.titleResolver),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Default.ArrowDropDown, null)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableStudyTypes.forEach {
                    PopupContentItem(
                        onClick = {
                            selectedType.value = it
                            expanded = false
                        }
                    ) {
                        Text(resolveString(it.titleResolver))
                    }
                }
            }
        }
    }

}

@Composable
fun GeneralDashboardReviewButton(
    onClick: () -> Unit,
    count: Int,
    text: String,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxHeight()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = count > 0, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light
        )

        if (count == 0) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier
                    .background(MaterialTheme.extraColorScheme.success, CircleShape)
                    .padding(4.dp),
                tint = Color.White
            )
        } else {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun GeneralDashboardNoDecksButton(
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxHeight()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = resolveString { "No decks" },
            fontSize = 14.sp,
            fontWeight = FontWeight.Light
        )

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "Create",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.KeyboardArrowRight, null)
        }


    }
}
