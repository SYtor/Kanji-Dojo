package ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentWithReceiverOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.buildFuriganaString
import ua.syt0r.kanji.core.app_data.data.withEmptyFurigana
import ua.syt0r.kanji.core.app_data.data.withoutAnnotations
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.jsonSaver
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme
import ua.syt0r.kanji.presentation.common.trackItemPosition
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.dialog.AddWordToDeckDialog
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationCharactersPreview
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationItemsSelector
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationOption
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeLeaveConfirmationDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSavedState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSavingResult
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSavingState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeToolbar
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeToolbarState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.rememberPracticeConfigurationItemsSelectorState
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ReadingPracticeContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ReadingPracticeSelectedOption
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ReadingScreenConfiguration

@Composable
fun ReadingPracticeScreenUI(
    state: State<ScreenState>,
    navigateBack: () -> Unit,
    onConfigured: (ReadingScreenConfiguration) -> Unit,
    onOptionSelected: (ReadingPracticeSelectedOption) -> Unit,
    toggleKanaAutoPlay: () -> Unit,
    speakKana: (KanaReading) -> Unit,
    onPracticeSaveClick: (PracticeSavingResult) -> Unit,
    onFinishButtonClick: () -> Unit,
    onWordFeedback: (JapaneseWord) -> Unit
) {

    var shouldShowLeaveConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    if (shouldShowLeaveConfirmationDialog) {
        PracticeLeaveConfirmationDialog(
            onDismissRequest = { shouldShowLeaveConfirmationDialog = false },
            onConfirmClick = { navigateBack() }
        )
    }

    val shouldShowLeaveConfirmationOnBackClick = remember {
        derivedStateOf {
            state.value.let { !(it is ScreenState.Configuration || it is ScreenState.Saved) }
        }
    }

    if (shouldShowLeaveConfirmationOnBackClick.value) {
        MultiplatformBackHandler { shouldShowLeaveConfirmationDialog = true }
    }

    var alternativeDialogWord by rememberSaveable(stateSaver = jsonSaver()) {
        mutableStateOf<JapaneseWord?>(null)
    }

    alternativeDialogWord?.let {
        AlternativeWordsDialog(
            word = it,
            onDismissRequest = { alternativeDialogWord = null },
            onFeedbackClick = { onWordFeedback(it) }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PracticeToolbar(
                state = state.toToolbarState(),
                onUpButtonClick = {
                    if (shouldShowLeaveConfirmationOnBackClick.value) {
                        shouldShowLeaveConfirmationDialog = true
                    } else {
                        navigateBack()
                    }
                }
            )
        }
    ) { paddingValues ->

        val transition = updateTransition(
            targetState = state.value,
            label = "Content Transition",
        )

        transition.AnimatedContent(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { screenState ->

            when (screenState) {
                ScreenState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                    )
                }

                is ScreenState.Configuration -> {
                    val characterSelectionState = rememberPracticeConfigurationItemsSelectorState(
                        characters = screenState.characters,
                        shuffle = true
                    )
                    var kanaRomaji by remember { mutableStateOf(screenState.kanaRomaji) }
                    PracticeConfigurationContainer(
                        onClick = {
                            onConfigured(
                                ReadingScreenConfiguration(
                                    characters = characterSelectionState.result,
                                    kanaRomaji = kanaRomaji
                                )
                            )
                        }
                    ) {
                        PracticeConfigurationItemsSelector(
                            state = characterSelectionState
                        )

                        PracticeConfigurationCharactersPreview(
                            characters = characterSelectionState.sortedList.value,
                            selectedCharactersCount = characterSelectionState.selectedCountState
                        )

                        PracticeConfigurationOption(
                            title = resolveString { readingPractice.kanaRomajiTitle },
                            subtitle = resolveString { readingPractice.kanaRomajiMessage },
                            checked = kanaRomaji,
                            onChange = { kanaRomaji = it }
                        )
                    }
                }

                is ScreenState.Review -> {
                    Review(
                        state = screenState,
                        onOptionSelected = onOptionSelected,
                        toggleKanaAutoPlay = toggleKanaAutoPlay,
                        speakKana = speakKana,
                        onWordClick = { alternativeDialogWord = it }
                    )
                }

                is ScreenState.Saving -> {
                    PracticeSavingState(
                        defaultToleratedMistakesCount = screenState.toleratedMistakesCount,
                        reviewResults = screenState.reviewResultList,
                        onSaveClick = onPracticeSaveClick
                    )
                }

                is ScreenState.Saved -> {
                    PracticeSavedState(
                        charactersReviewed = screenState.run { goodCharacters.size + repeatCharacters.size },
                        practiceDuration = screenState.practiceDuration,
                        accuracy = screenState.accuracy,
                        failedCharacters = screenState.repeatCharacters,
                        goodCharacters = screenState.goodCharacters,
                        onFinishClick = onFinishButtonClick
                    )
                }
            }

        }

    }

}

@Composable
private fun Review(
    state: ScreenState.Review,
    onOptionSelected: (ReadingPracticeSelectedOption) -> Unit,
    toggleKanaAutoPlay: () -> Unit,
    speakKana: (KanaReading) -> Unit,
    onWordClick: (JapaneseWord) -> Unit
) {

    val reviewState = state.data.collectAsState()

    val contentBottomPadding = remember { mutableStateOf(16.dp) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val reviewData = reviewState.value

        val characterDetailsContent = movableContentWithReceiverOf<ColumnScope> {
            ReadingPracticeCharacterDetailsUI(
                data = reviewData.characterData,
                showAnswer = reviewData.showAnswer,
                kanaAutoPlay = reviewData.kanaVoiceAutoPlay,
                toggleKanaAutoPlay = toggleKanaAutoPlay,
                speakKana = speakKana
            )
        }

        val wordsContent = movableContentWithReceiverOf<ColumnScope> {
            WordsSection(
                words = reviewData.characterData.words,
                showAnswerState = reviewData.showAnswer,
                onWordClick = onWordClick
            )
        }

        if (LocalOrientation.current == Orientation.Portrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = contentBottomPadding.value)
            ) {
                characterDetailsContent()
                wordsContent()
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = contentBottomPadding.value)
                ) {
                    characterDetailsContent()
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 10.dp, end = 20.dp)
                        .padding(bottom = contentBottomPadding.value)
                ) {
                    wordsContent()
                }

            }

        }

        ButtonsSection(
            onOptionSelected = onOptionSelected,
            shouldShowAnswer = reviewData.showAnswer,
            contentBottomPadding = contentBottomPadding
        )

    }

}

@Composable
private fun ColumnScope.WordsSection(
    words: List<JapaneseWord>,
    showAnswerState: State<Boolean>,
    onWordClick: (JapaneseWord) -> Unit
) {

    Text(
        text = resolveString { readingPractice.words },
        modifier = Modifier.padding(vertical = 8.dp),
        style = MaterialTheme.typography.titleMedium
    )

    var wordToAddToVocabDeck by remember { mutableStateOf<JapaneseWord?>(null) }
    wordToAddToVocabDeck?.let {
        AddWordToDeckDialog(
            wordId = it.id,
            wordPreviewReading = it.readings.first().withoutAnnotations(),
            onDismissRequest = { wordToAddToVocabDeck = null }
        )
    }

    val showAnswer = showAnswerState.value
    words.forEachIndexed { index, word ->

        val message = if (showAnswer) {
            word.orderedPreview(index)
        } else {
            buildFuriganaString {
                append("${index + 1}. ")
                append(word.readings.first().withEmptyFurigana())
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .padding(horizontal = 12.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable(enabled = showAnswer) { onWordClick(word) }
                .padding(vertical = 4.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FuriganaText(
                furiganaString = message,
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (showAnswer)
                IconButton(onClick = { wordToAddToVocabDeck = word }) {
                    Icon(Icons.Default.AddCircleOutline, null)
                }
        }

    }

}

@Composable
private fun BoxScope.ButtonsSection(
    onOptionSelected: (ReadingPracticeSelectedOption) -> Unit,
    shouldShowAnswer: State<Boolean>,
    contentBottomPadding: MutableState<Dp>
) {

    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp)
            .widthIn(max = 400.dp)
            .clip(CircleShape)
            .trackItemPosition { contentBottomPadding.value = it.heightFromScreenBottom + 16.dp }
    ) {
        if (shouldShowAnswer.value) {
            OptionButton(
                title = resolveString { readingPractice.repeatButton },
                onClick = { onOptionSelected(ReadingPracticeSelectedOption.Repeat) },
                containerColor = MaterialTheme.colorScheme.primary
            )
            Box(
                modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .width(2.dp)
                    .background(MaterialTheme.colorScheme.surface)
            )
            OptionButton(
                title = resolveString { readingPractice.goodButton },
                onClick = { onOptionSelected(ReadingPracticeSelectedOption.Good) },
                containerColor = MaterialTheme.extraColorScheme.success
            )
        } else {
            OptionButton(
                title = resolveString { readingPractice.showAnswerButton },
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { onOptionSelected(ReadingPracticeSelectedOption.RevealAnswer) }
            )
        }
    }

}

@Composable
private fun RowScope.OptionButton(
    title: String,
    onClick: () -> Unit,
    containerColor: Color,
    textColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = textColor,
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .background(containerColor)
            .padding(vertical = 12.dp)
            .wrapContentSize()
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
private fun State<ScreenState>.toToolbarState(): State<PracticeToolbarState> {
    val state = remember { mutableStateOf<PracticeToolbarState>(PracticeToolbarState.Loading) }
    LaunchedEffect(Unit) {
        snapshotFlow { value }
            .flatMapLatest { screenState ->
                when (screenState) {
                    ScreenState.Loading -> flowOf(PracticeToolbarState.Loading)
                    is ScreenState.Configuration -> flowOf(PracticeToolbarState.Configuration)
                    is ScreenState.Review -> screenState.data.map {
                        it.progress.run {
                            PracticeToolbarState.Review(pendingCount, repeatCount, finishedCount)
                        }
                    }

                    is ScreenState.Saving -> flowOf(PracticeToolbarState.Saving)
                    is ScreenState.Saved -> flowOf(PracticeToolbarState.Saved)
                }
            }
            .collect { state.value = it }
    }
    return state
}
