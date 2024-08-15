package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.buildFuriganaString
import ua.syt0r.kanji.core.srs.SrsCard
import ua.syt0r.kanji.presentation.common.AutopaddedScrollableColumn
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.MultiplatformDialog
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.neutralButtonColors
import ua.syt0r.kanji.presentation.common.theme.snapToBiggerContainerCrossfadeTransitionSpec
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationEnumSelector
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationItemsSelector
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationOption
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeProgressCounter
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSavedStateInfoLabel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeReadingPriority
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabSummaryItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui.VocabPracticeFlashcardUI
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui.VocabPracticeReadingPickerUI
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.ui.VocabPracticeWritingUI

@Composable
fun VocabPracticeScreenUI(
    state: State<ScreenState>,
    onConfigured: () -> Unit,
    onFlashcardAnswerRevealClick: () -> Unit,
    onReadingPickerAnswerSelected: (String) -> Unit,
    onNext: (SrsCard) -> Unit,
    onFeedback: (JapaneseWord) -> Unit,
    navigateBack: () -> Unit,
    finishPractice: () -> Unit
) {

    var showPracticeFinishDialog by rememberSaveable { mutableStateOf(false) }
    if (showPracticeFinishDialog) {
        PracticeEarlyFinishDialog(
            onDismissRequest = { showPracticeFinishDialog = false },
            onConfirmClick = {
                finishPractice()
                showPracticeFinishDialog = false
            }
        )
    }

    val tryNavigateBack = {
        val isSafeToLeave = state.value.let {
            it is ScreenState.Configuration || it is ScreenState.Summary
        }

        if (isSafeToLeave) navigateBack()
        else showPracticeFinishDialog = true
    }

    MultiplatformBackHandler(onBack = tryNavigateBack)

    Scaffold(
        topBar = { ScreenTopBar(navigateUp = tryNavigateBack, state = state) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.value,
            transitionSpec = snapToBiggerContainerCrossfadeTransitionSpec(),
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {

            when (it) {
                ScreenState.Loading -> {
                    FancyLoading(Modifier.fillMaxSize().wrapContentSize())
                }

                is ScreenState.Configuration -> {
                    ScreenConfiguration(
                        screenState = it,
                        onConfigured = onConfigured
                    )
                }

                is ScreenState.Review -> {
                    ScreenReview(
                        screenState = it,
                        onFlashcardAnswerRevealClick = onFlashcardAnswerRevealClick,
                        onAnswerSelected = onReadingPickerAnswerSelected,
                        onNextClick = onNext,
                        onFeedbackClick = onFeedback
                    )
                }

                is ScreenState.Summary -> {
                    ScreenSummary(
                        screenState = it,
                        onFinishClick = navigateBack
                    )
                }
            }

        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenTopBar(
    navigateUp: () -> Unit,
    state: State<ScreenState>
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        },
        actions = {
            val progress = remember {
                derivedStateOf {
                    when (val practiceState = state.value) {
                        is ScreenState.Review -> practiceState.state.value.progress
                        else -> null
                    }
                }
            }

            AnimatedContent(
                targetState = progress.value,
                contentKey = { it == null },
                transitionSpec = {
                    fadeIn() togetherWith fadeOut() using SizeTransform { _, _ -> snap() }
                }
            ) {
                if (it == null) return@AnimatedContent
                PracticeProgressCounter(it.pending, it.repeats, it.completed)
            }
        }
    )
}

@Composable
private fun ScreenConfiguration(
    screenState: ScreenState.Configuration,
    onConfigured: () -> Unit
) {

    PracticeConfigurationContainer(onClick = onConfigured) {

        PracticeConfigurationItemsSelector(
            state = screenState.itemsSelectorState
        )

        var readingPriority by screenState.readingPriority
        PracticeConfigurationEnumSelector(
            title = resolveString { vocabPractice.readingPriorityConfigurationTitle },
            subtitle = resolveString { vocabPractice.readingPriorityConfigurationMessage },
            values = VocabPracticeReadingPriority.values(),
            selected = readingPriority,
            onSelected = { readingPriority = it }
        )

        when (screenState.practiceType) {
            VocabPracticeType.Flashcard -> {
                var translationInFront by screenState.flashcard.translationInFront
                PracticeConfigurationOption(
                    title = resolveString { vocabPractice.translationInFrontConfigurationTitle },
                    subtitle = resolveString { vocabPractice.translationInFrontConfigurationMessage },
                    checked = translationInFront,
                    onChange = { translationInFront = it }
                )
            }

            VocabPracticeType.ReadingPicker -> {
                var showMeaning by screenState.readingPicker.showMeaning
                PracticeConfigurationOption(
                    title = resolveString { vocabPractice.readingMeaningConfigurationTitle },
                    subtitle = resolveString { vocabPractice.readingMeaningConfigurationMessage },
                    checked = showMeaning,
                    onChange = { showMeaning = it }
                )
            }

            VocabPracticeType.Writing -> {

            }
        }

    }

}

@Composable
private fun ScreenReview(
    screenState: ScreenState.Review,
    onFlashcardAnswerRevealClick: () -> Unit,
    onAnswerSelected: (String) -> Unit,
    onNextClick: (SrsCard) -> Unit,
    onFeedbackClick: (JapaneseWord) -> Unit
) {

    var alternativeWordsDialogWord by remember { mutableStateOf<JapaneseWord?>(null) }
    alternativeWordsDialogWord?.also {
        AlternativeWordsDialog(
            word = it,
            onDismissRequest = { alternativeWordsDialogWord = null }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val reviewState = screenState.state.value
        when (val currentState = reviewState.reviewState) {
            is VocabReviewState.Flashcard -> {
                VocabPracticeFlashcardUI(
                    reviewState = currentState,
                    answers = reviewState.answers,
                    onRevealAnswerClick = onFlashcardAnswerRevealClick,
                    onNextClick = onNextClick,
                    onWordClick = { alternativeWordsDialogWord = it },
                )
            }

            is VocabReviewState.Reading -> {
                VocabPracticeReadingPickerUI(
                    reviewState = currentState,
                    answers = reviewState.answers,
                    onWordClick = { alternativeWordsDialogWord = it },
                    onAnswerSelected = onAnswerSelected,
                    onNextClick = onNextClick,
                    onFeedbackClick = onFeedbackClick
                )
            }

            is VocabReviewState.Writing -> {
                VocabPracticeWritingUI(
                    reviewState = currentState,
                    answers = reviewState.answers,
                    onNextClick = onNextClick,
                    onWordClick = { alternativeWordsDialogWord = it },
                    onFeedbackClick = onFeedbackClick
                )
            }
        }

    }

}

@Composable
private fun ScreenSummary(
    screenState: ScreenState.Summary,
    onFinishClick: () -> Unit
) {

    AutopaddedScrollableColumn(
        modifier = Modifier.fillMaxWidth()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp),
        bottomOverlayContent = {

            Button(
                onClick = onFinishClick,
                colors = ButtonDefaults.neutralButtonColors(),
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(0.4f))
                    .padding(vertical = 20.dp)
            ) {
                Text(text = resolveString { commonPractice.savedButton })
            }

        }
    ) {

        PracticeSavedStateInfoLabel(
            title = resolveString { commonPractice.savedTimeSpentLabel },
            data = resolveString {
                commonPractice.savedTimeSpentValue(screenState.practiceDuration)
            }
        )

        PracticeSavedStateInfoLabel(
            title = resolveString { vocabPractice.summaryItemsCountTitle },
            data = resolveString { screenState.results.size.toString() }
        )

        screenState.results.forEachIndexed { index, item ->
            SummaryItem(index, item)
            if (index != screenState.results.size - 1) HorizontalDivider()
        }

    }

}

@Composable
private fun SummaryItem(
    index: Int,
    item: VocabSummaryItem
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(IntrinsicSize.Min)
    ) {

        FuriganaText(
            furiganaString = buildFuriganaString {
                append("${index + 1}. ")
                append(item.reading)
            },
            modifier = Modifier
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = resolveString { vocabPractice.summaryNextReviewLabel },
                modifier = Modifier.weight(1f).alignByBaseline()
            )
            Text(
                text = resolveString { vocabPractice.formattedSrsInterval(item.nextInterval) },
                modifier = Modifier.alignByBaseline()
            )
        }

    }
}

@Composable
private fun PracticeEarlyFinishDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {

    val strings = resolveString { vocabPractice }

    MultiplatformDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = strings.earlyFinishDialogTitle,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        content = {
            Text(
                text = strings.earlyFinishDialogMessage,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(text = strings.earlyFinishDialogCancelButton)
            }
            TextButton(onClick = onConfirmClick) {
                Text(text = strings.earlyFinishDialogAcceptButton)
            }
        }
    )
}

