package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.jsonSaver
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
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
                ScreenState.Loading -> PracticeToolbarState.Loading
                is ScreenState.Configuring -> PracticeToolbarState.Configuration
                is ScreenState.Review -> currentState.practiceProgress.run {
                    PracticeToolbarState.Review(
                        pending = pending,
                        repeat = repeats,
                        completed = completed
                    )
                }

                is ScreenState.Summary -> PracticeToolbarState.Saved
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

    PracticeConfigurationContainer(
        onClick = onConfigurationCompleted
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

            is LetterPracticeConfiguration.Reading -> TODO()
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

        is LetterPracticeReviewState.Reading -> TODO()
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
                        text = "$index. ${item.letter}",
                        modifier = Modifier
                    )
                },
                nextInterval = item.nextInterval
            )
            if (index != screenState.items.size - 1) HorizontalDivider()
        }

    }


}
