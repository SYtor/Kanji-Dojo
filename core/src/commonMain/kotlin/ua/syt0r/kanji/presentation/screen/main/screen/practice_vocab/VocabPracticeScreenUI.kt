package ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.theme.neutralButtonColors
import ua.syt0r.kanji.presentation.common.theme.neutralTextButtonColors
import ua.syt0r.kanji.presentation.common.ui.FancyLoading
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSavedStateInfoLabel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.VocabPracticeScreenContract.ScreenState

@Composable
fun VocabPracticeScreenUI(
    state: State<ScreenState>,
    onConfigured: (VocabPracticeConfiguration) -> Unit,
    onAnswerSelected: (String) -> Unit,
    onNext: () -> Unit,
    navigateBack: () -> Unit
) {

    val tryNavigateBack = {
        // TODO leave confirmation
        navigateBack()
    }

    Scaffold(
        topBar = { ScreenTopBar(navigateUp = tryNavigateBack) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        AnimatedContent(
            targetState = state.value,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
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
                        onAnswerSelected = onAnswerSelected,
                        onNext = onNext
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
private fun ScreenTopBar(navigateUp: () -> Unit) {
    TopAppBar(
        title = { Text("") },
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        }
    )
}

@Composable
private fun ScreenConfiguration(
    screenState: ScreenState.Configuration,
    onConfigured: (VocabPracticeConfiguration) -> Unit
) {

    PracticeConfigurationContainer(
        onClick = {
            onConfigured(VocabPracticeConfiguration(practiceType = VocabPracticeType.ReadingPicker))
        }
    ) {

    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScreenReview(
    screenState: ScreenState.Review,
    onAnswerSelected: (String) -> Unit,
    onNext: () -> Unit
) {

    val reviewState = screenState.reviewState.collectAsState()

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

        when (val currentState = reviewState.value) {
            is VocabReviewState.Reading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                        .wrapContentWidth()
                        .widthIn(max = 400.dp)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    val selectedAnswer by currentState.selectedAnswer

                    FuriganaText(
                        furiganaString = currentState.displayReading.value,
                        textStyle = MaterialTheme.typography.displayLarge,
                        annotationTextStyle = MaterialTheme.typography.bodyLarge,
                    )

                    if (selectedAnswer != null)
                        TextButton(
                            onClick = { alternativeWordsDialogWord = currentState.word },
                            colors = ButtonDefaults.neutralTextButtonColors()
                        ) {
                            Text(currentState.word.meanings.first())
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                        }

                    Spacer(modifier = Modifier.weight(1f))

                    val maxItemsInEachRow = 4

                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        maxItemsInEachRow = maxItemsInEachRow
                    ) {

                        currentState.answers.forEach { answer ->
                            ReadingAnswerButton(
                                answer = answer,
                                selectedAnswer = selectedAnswer,
                                enabled = selectedAnswer == null,
                                onClick = { onAnswerSelected(answer) }
                            )
                        }

                        val lastLineItems = currentState.answers.size % 4
                        if (lastLineItems != 0)
                            Spacer(Modifier.weight(maxItemsInEachRow.toFloat() - lastLineItems))

                    }
                    val shouldShowNextButton = remember {
                        derivedStateOf {
                            when (val currentState = reviewState.value) {
                                is VocabReviewState.Reading -> currentState.selectedAnswer.value != null
                            }
                        }
                    }

                    NextButton(
                        showNextButton = shouldShowNextButton,
                        onClick = onNext
                    )
                }

            }

        }


    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.ReadingAnswerButton(
    answer: String,
    selectedAnswer: SelectedReadingAnswer?,
    enabled: Boolean,
    onClick: () -> Unit
) {

    val (textColor, containerColor) = MaterialTheme.run {
        when {
            answer == selectedAnswer?.correct -> {
                colorScheme.surface to extraColorScheme.success
            }

            answer == selectedAnswer?.selected && !selectedAnswer.isCorrect -> {
                colorScheme.surface to colorScheme.error
            }

            else -> {
                colorScheme.onSurface to colorScheme.surface
            }
        }
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            contentColor = textColor,
            disabledContentColor = textColor,
            containerColor = containerColor,
            disabledContainerColor = containerColor
        ),
        enabled = enabled,
        modifier = Modifier.weight(1f)
    ) {
        Text(text = answer, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun NextButton(
    showNextButton: State<Boolean>,
    onClick: () -> Unit
) {

    val offset = animateFloatAsState(if (showNextButton.value) 0f else 1f)

    Row(
        modifier = Modifier.graphicsLayer { translationY = size.height * offset.value }
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
            onClick = {},
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
            Text(text = "Next", style = MaterialTheme.typography.titleMedium)
            Icon(Icons.AutoMirrored.Filled.NavigateNext, null)
        }
    }

}

@Composable
private fun ScreenSummary(
    screenState: ScreenState.Summary,
    onFinishClick: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth()
            .wrapContentWidth()
            .widthIn(max = 400.dp)
            .padding(horizontal = 20.dp)
    ) {

        PracticeSavedStateInfoLabel(
            title = resolveString { commonPractice.savedTimeSpentLabel },
            data = resolveString {
                commonPractice.savedTimeSpentValue(screenState.practiceDuration)
            }
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onFinishClick,
            colors = ButtonDefaults.neutralButtonColors(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Text(text = resolveString { commonPractice.savedButton })
        }

    }

}
