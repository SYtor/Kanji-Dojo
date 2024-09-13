package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.resources.icon.Discord
import ua.syt0r.kanji.presentation.common.resources.icon.ExtraIcons
import ua.syt0r.kanji.presentation.common.resources.icon.YouTube
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.snapSizeTransform
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.common.ui.PopupContentItem
import ua.syt0r.kanji.presentation.dialog.VersionChangeDialog
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.GeneralDashboardScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.general_dashboard.ui.TutorialDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeScreenConfiguration

@Composable
fun GeneralDashboardScreenUI(
    state: State<ScreenState>,
    navigateToDailyLimitConfiguration: () -> Unit,
    navigateToCreateLetterDeck: () -> Unit,
    navigateToCreateVocabDeck: () -> Unit,
    navigateToLetterPractice: (MainDestination.LetterPractice) -> Unit,
    navigateToVocabPractice: (MainDestination.VocabPractice) -> Unit,
    youtubeClick: () -> Unit,
    discordClick: () -> Unit
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
                Text(resolveString { generalDashboard.buttonDailyLimit })
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

                Text(
                    text = resolveString { generalDashboard.buttonVersionChange },
                    modifier = Modifier.alignByBaseline()
                )
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
                Text(resolveString { generalDashboard.buttonTutorial }, Modifier.alignByBaseline())
                Icon(Icons.AutoMirrored.Outlined.HelpOutline, null)
            }

        },
        content = {

            val letterDecksTitle = resolveString { generalDashboard.letterDecksTitle }

            when (it.letterDecksData) {
                is LetterDecksData.Data -> {
                    DashboardItemLayout(
                        title = { Text(letterDecksTitle) },
                        middleContent = {
                            PracticeTypeSelector(
                                selectedType = it.letterDecksData.practiceType,
                                availablePracticeTypes = ScreenLetterPracticeType.values().toList(),
                            )
                        },
                        buttonsContent = {

                            val practiceType = it.letterDecksData.practiceType.value
                            val progress = it.letterDecksData.studyProgressMap
                                .getValue(practiceType)

                            val goToLetterPractice = { characterToDeckIdMap: Map<String, Long> ->
                                val destination = MainDestination.LetterPractice(
                                    configuration = LetterPracticeScreenConfiguration(
                                        characterToDeckIdMap = characterToDeckIdMap,
                                        practiceType = practiceType
                                    )
                                )
                                navigateToLetterPractice(destination)
                            }

                            GeneralDashboardReviewButton(
                                onClick = { goToLetterPractice(progress.newToDeckIdMap) },
                                count = progress.newToDeckIdMap.size,
                                text = resolveString { generalDashboard.buttonNew },
                                modifier = Modifier.weight(1f)
                            )

                            GeneralDashboardReviewButton(
                                onClick = { goToLetterPractice(progress.dueToDeckIdMap) },
                                count = progress.dueToDeckIdMap.size,
                                text = resolveString { generalDashboard.buttonDue },
                                modifier = Modifier.weight(1f)
                            )

                            GeneralDashboardReviewButton(
                                onClick = { goToLetterPractice(progress.combined) },
                                count = progress.combined.size,
                                text = resolveString { generalDashboard.buttonAll },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    )
                }

                LetterDecksData.NoDecks -> {
                    DashboardItemLayout(
                        title = { Text(letterDecksTitle) },
                        buttonsContent = {
                            GeneralDashboardNoDecksButton(
                                onClick = navigateToCreateLetterDeck,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    )
                }

            }

            val vocabDecksTitle = resolveString { generalDashboard.vocabDecksTitle }

            when (it.vocabDecksInfo) {
                is VocabDecksData.Data -> {

                    DashboardItemLayout(
                        title = { Text(vocabDecksTitle) },
                        middleContent = {
                            PracticeTypeSelector(
                                selectedType = it.vocabDecksInfo.practiceType,
                                availablePracticeTypes = ScreenVocabPracticeType.values().toList(),
                            )
                        },
                        buttonsContent = {

                            val practiceType = it.vocabDecksInfo.practiceType.value
                            val progress = it.vocabDecksInfo.studyProgressMap.getValue(practiceType)

                            val goToVocabPractice = { wordToDeckIdMap: Map<Long, Long> ->
                                val configuration = VocabPracticeScreenConfiguration(
                                    wordIdToDeckIdMap = wordToDeckIdMap,
                                    practiceType = practiceType
                                )
                                val destination = MainDestination.VocabPractice(configuration)
                                navigateToVocabPractice(destination)
                            }

                            GeneralDashboardReviewButton(
                                onClick = { goToVocabPractice(progress.newToDeckIdMap) },
                                count = progress.newToDeckIdMap.size,
                                text = resolveString { generalDashboard.buttonNew },
                                modifier = Modifier.weight(1f)
                            )

                            GeneralDashboardReviewButton(
                                onClick = { goToVocabPractice(progress.dueToDeckIdMap) },
                                count = progress.dueToDeckIdMap.size,
                                text = resolveString { generalDashboard.buttonDue },
                                modifier = Modifier.weight(1f)
                            )

                            GeneralDashboardReviewButton(
                                onClick = { goToVocabPractice(progress.combined) },
                                count = progress.combined.size,
                                text = resolveString { generalDashboard.buttonAll },
                                modifier = Modifier.weight(1f)
                            )

                        }
                    )

                }

                VocabDecksData.NoDecks -> {

                    DashboardItemLayout(
                        title = { Text(vocabDecksTitle) },
                        buttonsContent = {
                            GeneralDashboardNoDecksButton(
                                onClick = navigateToCreateVocabDeck,
                                modifier = Modifier
                            )
                        }
                    )
                }
            }

            DashboardItemLayout(
                title = {
                    Text(text = resolveString { generalDashboard.streakTitle })
                    StreakIndicator(it.currentStreak)
                },
                buttonsContent = {
                    Column(
                        modifier = Modifier.clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .weight(1f)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = resolveString { generalDashboard.currentStreakLabel },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = it.currentStreak.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.weight(1f)
                            )

                            if (it.currentStreak >= it.longestStreak && it.currentStreak != 0) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, null)
                            }
                        }

                    }

                    Column(
                        modifier = Modifier.clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .weight(1f)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = resolveString { generalDashboard.longestStreakLabel },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light
                        )

                        Text(
                            text = it.longestStreak.toString(),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                }
            )

            StreakCalendar(it.streakCalendarData)

            Spacer(Modifier.weight(1f))

            Row(
                modifier = ListContentModifier
                    .height(IntrinsicSize.Min)
                    .padding(top = 10.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {

                SocialIconButton(
                    imageVector = ExtraIcons.YouTube,
                    onClick = youtubeClick
                )

                SocialIconButton(
                    imageVector = ExtraIcons.Discord,
                    onClick = discordClick
                )

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

private val ListContentModifier = Modifier.fillMaxWidth()
    .wrapContentWidth()
    .width(400.dp)
    .padding(horizontal = 20.dp)

@Composable
private fun DashboardItemLayout(
    title: @Composable RowScope.() -> Unit,
    middleContent: (@Composable () -> Unit)? = null,
    buttonsContent: @Composable RowScope.() -> Unit
) {

    Column(
        modifier = ListContentModifier.padding(vertical = 8.dp),
    ) {

        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.headlineSmall
            ) {
                title()
            }
        }

        if (middleContent != null) middleContent.invoke()
        else Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            buttonsContent()

        }

    }
}

@Composable
private fun <T : ScreenPracticeType> PracticeTypeSelector(
    selectedType: MutableState<T>,
    availablePracticeTypes: List<T>,
) {

    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = resolveString { generalDashboard.practiceTypeLabel },
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
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                availablePracticeTypes.forEach {
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
            text = resolveString { generalDashboard.buttonNoDecksTitle },
            fontSize = 14.sp,
            fontWeight = FontWeight.Light
        )

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = resolveString { generalDashboard.buttonNoDecksMessage },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }


    }
}


@Composable
private fun SocialIconButton(imageVector: ImageVector, onClick: () -> Unit) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier.aspectRatio(1f, true)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(4.dp),
        tint = MaterialTheme.colorScheme.onSurface
    )
}

private val WeekDayLabels = listOf("月", "火", "水", "木", "金", "土", "日")

@Composable
private fun StreakCalendar(items: List<StreakCalendarItem>) {
    Row(
        modifier = ListContentModifier
            .clip(
                MaterialTheme.shapes.medium.copy(
                    topStart = CornerSize(0),
                    bottomStart = CornerSize(0)
                )
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {


        items.forEach { (date, anyReviews) ->

            val bgColor: Color
            val textColor: Color

            when {
                anyReviews -> {
                    bgColor = MaterialTheme.colorScheme.primary
                    textColor = MaterialTheme.colorScheme.onPrimary
                }

                else -> {
                    bgColor = MaterialTheme.colorScheme.surfaceVariant
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight()
                    .weight(1f)
                    .wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    Modifier
                        .background(bgColor, MaterialTheme.shapes.medium)
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .wrapContentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = WeekDayLabels[date.dayOfWeek.value - 1],
                        modifier = Modifier,
                        color = textColor,
                        style = TextStyle(
                            lineHeightStyle = LineHeightStyle(
                                LineHeightStyle.Alignment.Center,
                                LineHeightStyle.Trim.Both
                            ),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    )
                }
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelSmall
                )
            }

        }

    }
}

@Composable
private fun StreakIndicator(currentStreakLength: Int) {
    val transition = rememberInfiniteTransition()
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse)
    )

    val innerCircleColor = lerp(
        start = MaterialTheme.colorScheme.surfaceVariant.convert(ColorSpaces.Oklab),
        stop = MaterialTheme.colorScheme.primary.convert(ColorSpaces.Oklab),
        fraction = when (currentStreakLength) {
            0 -> 0f
            1, 2 -> 0.6f
            3, 4 -> 0.75f
            5, 6 -> 0.9f
            else -> 1f
        }
    )
    val outerCircleColor = innerCircleColor.copy(innerCircleColor.alpha * 0.3f)

    Canvas(
        modifier = Modifier.aspectRatio(1f, true)
    ) {

        val innerCircleRadius = size.maxDimension * 0.3f
        val outerCircleRadius =
            size.maxDimension * 0.35f + size.maxDimension * 0.1f * progress.value

        drawCircle(outerCircleColor, outerCircleRadius)
        drawCircle(innerCircleColor, innerCircleRadius)

    }
}
