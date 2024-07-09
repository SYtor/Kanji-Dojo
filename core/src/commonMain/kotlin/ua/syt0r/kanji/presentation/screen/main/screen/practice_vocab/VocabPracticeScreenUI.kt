package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.buildFuriganaString
import ua.syt0r.kanji.core.srs.SrsItemData
import ua.syt0r.kanji.presentation.common.AutopaddedScrollableColumn
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.neutralButtonColors
import ua.syt0r.kanji.presentation.common.theme.snappyCrossfadeTransitionSpec
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationEnumSelector
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationItemsSelector
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationOption
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeLeaveConfirmationDialog
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
import kotlin.time.Duration

@Composable
fun VocabPracticeScreenUI(
    state: State<ScreenState>,
    onConfigured: () -> Unit,
    onFlashcardAnswerRevealClick: () -> Unit,
    onReadingPickerAnswerSelected: (String) -> Unit,
    onNext: (SrsItemData) -> Unit,
    onFeedback: (JapaneseWord) -> Unit,
    navigateBack: () -> Unit
) {

    var showLeaveConfirmation by rememberSaveable { mutableStateOf(false) }
    if (showLeaveConfirmation) {
        PracticeLeaveConfirmationDialog(
            onDismissRequest = { showLeaveConfirmation = false },
            onConfirmClick = navigateBack
        )
    }

    val tryNavigateBack = {
        val isSafeToLeave = state.value.let {
            it is ScreenState.Configuration || it is ScreenState.Summary
        }

        if (isSafeToLeave) navigateBack()
        else showLeaveConfirmation = true
    }

    Scaffold(
        topBar = { ScreenTopBar(navigateUp = tryNavigateBack, state = state) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.value,
            transitionSpec = snappyCrossfadeTransitionSpec(),
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

        PracticeConfigurationEnumSelector(
            title = resolveString { vocabPractice.practiceTypeConfigurationTitle },
            subtitle = resolveString { vocabPractice.practiceTypeConfigurationMessage },
            values = VocabPracticeType.values(),
            selected = screenState.practiceType.value,
            onSelected = { screenState.practiceType.value = it }
        )

        var readingPriority by screenState.readingPriority
        PracticeConfigurationEnumSelector(
            title = resolveString { vocabPractice.readingPriorityConfigurationTitle },
            subtitle = resolveString { vocabPractice.readingPriorityConfigurationMessage },
            values = VocabPracticeReadingPriority.values(),
            selected = readingPriority,
            onSelected = { readingPriority = it }
        )

        when (screenState.practiceType.value) {
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
    onNextClick: (SrsItemData) -> Unit,
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
                    onWordClick = { alternativeWordsDialogWord = it },
                    onAnswerSelected = onAnswerSelected,
                    onNextClick = { onNextClick(reviewState.answers.good) },
                    onFeedbackClick = onFeedbackClick
                )
            }

            is VocabReviewState.Writing -> {
                VocabPracticeWritingUI(
                    reviewState = currentState,
                    onNextClick = { onNextClick(reviewState.answers.good) },
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
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

        screenState.results.forEachIndexed { index, item -> SummaryItem(index, item) }

    }

}

@Composable
private fun SummaryItem(
    index: Int,
    item: VocabSummaryItem
) {
    when (item) {
        is VocabSummaryItem.Flashcard -> {
            FuriganaText(
                furiganaString = buildFuriganaString {
                    append("${index + 1}. ")
                    append(item.reading)
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
        }

        is VocabSummaryItem.ReadingPicker -> {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {

                FuriganaText(
                    furiganaString = buildFuriganaString {
                        append("${index + 1}. ")
                        append(item.reading)
                    },
                    modifier = Modifier.weight(1f)
                )

                SummaryCharacterResult(
                    character = item.character,
                    isCorrect = item.isCorrect
                )

            }
        }

        is VocabSummaryItem.Writing -> {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FuriganaText(
                    furiganaString = buildFuriganaString {
                        append("${index + 1}. ")
                        append(item.reading)
                    }
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Spacer(Modifier.width(20.dp))
                    item.results.forEach { SummaryCharacterResult(it.character, it.isCorrect) }
                }
            }
        }
    }

}

@Composable
private fun SummaryCharacterResult(character: String, isCorrect: Boolean) {
    Text(
        text = character,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.clip(MaterialTheme.shapes.small)
            .clickable { }
            .border(
                width = 2.dp,
                color = when {
                    isCorrect -> MaterialTheme.extraColorScheme.success
                    else -> MaterialTheme.colorScheme.error
                },
                shape = MaterialTheme.shapes.small
            )
            .padding(vertical = 8.dp, horizontal = 12.dp)
    )
}


@Composable
fun VocabPracticeNextButton(
    showNextButton: State<Boolean>,
    onClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val offset = animateFloatAsState(if (showNextButton.value) 0f else 1f)

    Row(
        modifier = modifier.graphicsLayer { translationY = size.height * offset.value }
            .fillMaxWidth()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp)
            .height(IntrinsicSize.Min)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onFeedbackClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxHeight()
                .aspectRatio(1f)
        ) {
            Icon(Icons.Default.Flag, null)
        }

        Button(
            onClick = onClick,
            colors = ButtonDefaults.neutralButtonColors(),
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Text(
                text = resolveString { vocabPractice.nextButton },
                style = MaterialTheme.typography.titleMedium
            )
            Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
        }
    }

}

fun srsFormatDuration(duration: Duration): String = when {
    duration.inWholeDays > 0 -> buildString {
        append("${duration.inWholeDays}d")
        appendIfNot0(duration.inWholeHours % 24) { " ${it}h" }
    }

    duration.inWholeHours > 0 -> buildString {
        append("${duration.inWholeHours}h")
        appendIfNot0(duration.inWholeMinutes % 60) { " ${it}m" }
    }

    duration.inWholeMinutes > 0 -> buildString {
        append("${duration.inWholeMinutes}m")
        appendIfNot0(duration.inWholeSeconds % 60) { " ${it}s" }
    }

    else -> "${duration.inWholeSeconds}s"
}

private fun StringBuilder.appendIfNot0(number: Long, text: (Long) -> String) {
    if (number != 0L) append(text(number))
}

