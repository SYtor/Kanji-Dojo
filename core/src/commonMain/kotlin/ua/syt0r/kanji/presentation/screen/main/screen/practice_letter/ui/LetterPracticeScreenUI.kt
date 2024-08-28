package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.jsonSaver
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationCharactersPreview
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationEnumSelector
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationItemsSelector
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationOption
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeEarlyFinishDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryInfoLabel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeToolbar
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeToolbarState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.LetterPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeHintMode
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.WritingPracticeInputMode

@Composable
fun LetterPracticeScreenUI(
    state: State<ScreenState>,
    navigateBack: () -> Unit,
    navigateToWordFeedback: (JapaneseWord) -> Unit,
    onConfigured: () -> Unit,
    speakKana: (KanaReading) -> Unit,
    onNextClick: (PracticeAnswer) -> Unit,
    finishPractice: () -> Unit,
    onPracticeCompleted: () -> Unit
) {

    var showEarlyFinishDialog by rememberSaveable { mutableStateOf(false) }
    if (showEarlyFinishDialog) {
        PracticeEarlyFinishDialog(
            onDismissRequest = { showEarlyFinishDialog = false },
            onConfirmClick = {
                showEarlyFinishDialog = false
                finishPractice()
            }
        )
    }

    val shouldShowLeaveConfirmationOnBackClick = remember {
        derivedStateOf {
            state.value.let { !(it is ScreenState.Configuring || it is ScreenState.Summary) }
        }
    }

    if (shouldShowLeaveConfirmationOnBackClick.value) {
        MultiplatformBackHandler { showEarlyFinishDialog = true }
    }

    var selectedWordForAlternativeDialog by rememberSaveable(stateSaver = jsonSaver()) {
        mutableStateOf<JapaneseWord?>(null)
    }

    selectedWordForAlternativeDialog?.let {
        AlternativeWordsDialog(
            word = it,
            onDismissRequest = { selectedWordForAlternativeDialog = null },
            onFeedbackClick = { navigateToWordFeedback(it) }
        )
    }

    ScreenLayout(
        state = state,
        toolbar = {
            PracticeToolbar(
                state = state.toToolbarState(),
                onUpButtonClick = {
                    if (shouldShowLeaveConfirmationOnBackClick.value) {
                        showEarlyFinishDialog = true
                    } else {
                        navigateBack()
                    }
                }
            )
        },
        configuration = {
            ConfiguringState(
                state = it,
                onConfigurationCompleted = onConfigured
            )
        },
        review = {
            ReviewState(
                reviewState = it.reviewState,
                onNextClick = onNextClick,
                speakKana = speakKana,
                onWordClick = { selectedWordForAlternativeDialog = it }
            )
        },
        summary = {
            SummaryState(
                screenState = it,
                onFinishClick = onPracticeCompleted
            )
        }
    )

}

@Composable
private fun ScreenLayout(
    state: State<ScreenState>,
    toolbar: @Composable () -> Unit,
    configuration: @Composable (ScreenState.Configuring) -> Unit,
    review: @Composable (ScreenState.Review) -> Unit,
    summary: @Composable (ScreenState.Summary) -> Unit
) {

    Scaffold(
        topBar = { toolbar() }
    ) { paddingValues ->

        val transition = updateTransition(targetState = state.value, label = "AnimatedContent")
        transition.AnimatedContent(
            transitionSpec = {
                fadeIn(tween(600)) togetherWith fadeOut(tween(600))
            },
            contentKey = { it is ScreenState.Review },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { screenState ->

            when (screenState) {
                ScreenState.Loading -> FancyLoading(Modifier.fillMaxSize().wrapContentSize())
                is ScreenState.Configuring -> configuration(screenState)
                is ScreenState.Review -> review(screenState)
                is ScreenState.Summary -> summary(screenState)
            }

        }

    }

}

@Composable
private fun State<ScreenState>.toToolbarState(): State<PracticeToolbarState> {
    val state = remember {
        derivedStateOf {
            when (val currentState = value) {
                ScreenState.Loading,
                is ScreenState.Summary -> PracticeToolbarState.Idle

                is ScreenState.Configuring -> PracticeToolbarState.Configuration

                is ScreenState.Review -> PracticeToolbarState.Review(
                    practiceQueueProgress = currentState.practiceProgress
                )

            }
        }
    }
    return state
}

@Composable
private fun ConfiguringState(
    state: ScreenState.Configuring,
    onConfigurationCompleted: () -> Unit
) {

    val strings = resolveString { writingPractice }

    val practiceTypeTitle = resolveString(state.configuration.practiceType.titleResolver)

    PracticeConfigurationContainer(
        onClick = onConfigurationCompleted,
        practiceTypeMessage = "Letter Practice・$practiceTypeTitle"
    ) {

        when (val configuration = state.configuration) {
            is LetterPracticeConfiguration.Writing -> {

                var noTranslationLayout by configuration.noTranslationsLayout
                var leftHandedMode by configuration.leftHandedMode
                var kanaRomaji by configuration.useRomajiForKanaWords
                var altStrokeEvaluatorEnabled by configuration.altStrokeEvaluatorEnabled
                var selectedHintMode by configuration.hintMode
                var selectedInputMode by configuration.inputMode

                PracticeConfigurationItemsSelector(
                    state = configuration.selectorState
                )

                PracticeConfigurationCharactersPreview(
                    characters = configuration.selectorState.sortedList.value.map { it.first },
                    selectedCharactersCount = configuration.selectorState.selectedCountState
                )

                PracticeConfigurationEnumSelector(
                    title = resolveString { writingPractice.hintStrokesTitle },
                    subtitle = resolveString { writingPractice.hintStrokesMessage },
                    values = WritingPracticeHintMode.values(),
                    selected = selectedHintMode,
                    onSelected = { selectedHintMode = it }
                )

                PracticeConfigurationEnumSelector(
                    title = resolveString { writingPractice.inputModeTitle },
                    subtitle = resolveString { writingPractice.inputModeMessage },
                    values = WritingPracticeInputMode.values(),
                    selected = selectedInputMode,
                    onSelected = { selectedInputMode = it }
                )

                PracticeConfigurationOption(
                    title = strings.kanaRomajiTitle,
                    subtitle = strings.kanaRomajiMessage,
                    checked = kanaRomaji,
                    onChange = { kanaRomaji = it }
                )

                PracticeConfigurationOption(
                    title = strings.noTranslationLayoutTitle,
                    subtitle = strings.noTranslationLayoutMessage,
                    checked = noTranslationLayout,
                    onChange = { noTranslationLayout = it }
                )

                PracticeConfigurationOption(
                    title = strings.leftHandedModeTitle,
                    subtitle = strings.leftHandedModeMessage,
                    checked = leftHandedMode,
                    onChange = { leftHandedMode = it }
                )

                PracticeConfigurationOption(
                    title = strings.altStrokeEvaluatorTitle,
                    subtitle = strings.altStrokeEvaluatorMessage,
                    checked = altStrokeEvaluatorEnabled,
                    onChange = { altStrokeEvaluatorEnabled = it }
                )
            }

            is LetterPracticeConfiguration.Reading -> {

                PracticeConfigurationItemsSelector(
                    state = configuration.selectorState
                )

                PracticeConfigurationCharactersPreview(
                    characters = configuration.selectorState.sortedList.value.map { it.first },
                    selectedCharactersCount = configuration.selectorState.selectedCountState
                )

                PracticeConfigurationOption(
                    title = resolveString { readingPractice.kanaRomajiTitle },
                    subtitle = resolveString { readingPractice.kanaRomajiMessage },
                    checked = configuration.useRomajiForKanaWords.value,
                    onChange = { configuration.useRomajiForKanaWords.value = it }
                )

            }
        }

    }

}

@Composable
private fun ReviewState(
    reviewState: LetterPracticeReviewState,
    onNextClick: (PracticeAnswer) -> Unit,
    speakKana: (KanaReading) -> Unit,
    onWordClick: (JapaneseWord) -> Unit
) {

    when (reviewState) {
        is LetterPracticeReviewState.Writing -> LetterPracticeWritingUI(
            layoutConfiguration = reviewState.layout,
            reviewState = reviewState,
            onNextClick = onNextClick,
            speakKana = speakKana,
            onWordClick = onWordClick
        )

        is LetterPracticeReviewState.Reading -> LetterPracticeReadingUI(
            reviewState = reviewState,
            onNextClick = onNextClick,
            speakKana = speakKana,
            onWordClick = onWordClick
        )
    }

}

@Composable
private fun SummaryState(
    screenState: ScreenState.Summary,
    onFinishClick: () -> Unit
) {

    val strings = resolveString { commonPractice }

    PracticeSummaryContainer(onFinishClick) {

        PracticeSummaryInfoLabel(
            title = strings.savedReviewedCountLabel,
            data = screenState.items.size.toString()
        )

        PracticeSummaryInfoLabel(
            title = strings.savedTimeSpentLabel,
            data = strings.savedTimeSpentValue(screenState.duration)
        )

        if (screenState.accuracy != null)
            PracticeSummaryInfoLabel(
                title = strings.savedAccuracyLabel,
                data = "%.2f%%".format(screenState.accuracy)
            )

        screenState.items.forEachIndexed { index, item ->
            PracticeSummaryItem(
                header = {
                    Text(
                        text = "${index + 1}. ${item.letter}",
                        modifier = Modifier
                    )
                },
                nextInterval = item.nextInterval
            )
            if (index != screenState.items.size - 1) HorizontalDivider()
        }

    }

}

@Composable
fun KanjiVariantsRow(
    variants: String,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(text = resolveString { writingPractice.variantsTitle })

        val showVariants = remember { mutableStateOf(false) }

        val overlayAlpha = animateFloatAsState(targetValue = if (showVariants.value) 0f else 1f)

        val overlayColor = MaterialTheme.colorScheme.surfaceVariant
            .copy(alpha = overlayAlpha.value)
        val hintTextColor = MaterialTheme.colorScheme.onSurface
            .copy(alpha = overlayAlpha.value)
        val variantsTextColor = MaterialTheme.colorScheme.onSurface
            .copy(alpha = 1f - overlayAlpha.value)

        Box(
            modifier = Modifier.clip(MaterialTheme.shapes.small)
                .clickable { showVariants.value = !showVariants.value }
                .background(overlayColor)
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Text(
                text = resolveString { writingPractice.variantsHint },
                color = hintTextColor,
                maxLines = 1
            )
            Text(
                text = variants,
                color = variantsTextColor
            )
        }

    }

}

@Composable
fun LetterPracticeWordRow(
    index: Int,
    word: JapaneseWord,
    onWordClick: (JapaneseWord) -> Unit,
    addWordToDeckClick: (JapaneseWord) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .padding(horizontal = 12.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable { onWordClick(word) }
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FuriganaText(
            furiganaString = word.orderedPreview(index),
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { addWordToDeckClick(word) }) {
            Icon(Icons.Default.AddCircleOutline, null)
        }
    }
}
