package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.formattedVocabDefinition
import ua.syt0r.kanji.core.app_data.data.withEncodedText
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.FuriganaWordHeadline
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.trackItemPosition
import ua.syt0r.kanji.presentation.common.ui.FuriganaText
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Material3BottomSheetScaffold
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.CharacterWritingProgress
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswerButtonsContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswerButtonsRow
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswers
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeExampleWord
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeLayoutConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState

@Composable
fun LetterPracticeWritingUI(
    layoutConfiguration: LetterPracticeLayoutConfiguration.WritingLayoutConfiguration,
    reviewState: LetterPracticeReviewState.Writing,
    onNextClick: (PracticeAnswer) -> Unit,
    speakKana: (KanaReading) -> Unit,
    onWordClick: (JapaneseWord) -> Unit
) {

    val reviewState = rememberUpdatedState(reviewState)

    val infoSectionState = reviewState.asInfoSectionState(layoutConfiguration)
    val wordsBottomSheetState = reviewState.asWordsBottomSheetState()

    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    if (scaffoldState.bottomSheetState.isExpanded) {
        MultiplatformBackHandler {
            coroutineScope.launch { scaffoldState.bottomSheetState.collapse() }
        }
    }

    val bottomSheetHeightState = remember { mutableStateOf(100.dp) }

    val openBottomSheet: () -> Unit = {
        coroutineScope.launch { scaffoldState.bottomSheetState.expand() }
    }

    val answersSection: @Composable BoxScope.() -> Unit = {
        AnswerButtons(
            letterWritingButtonsState = reviewState.toAnswerButtonsState(),
            studyCompleted = { reviewState.value.isStudyMode.value = false },
            answerSelected = onNextClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (LocalOrientation.current == Orientation.Portrait) {

        Material3BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                LetterPracticeWritingWordsBottomSheet(
                    state = wordsBottomSheetState,
                    sheetContentHeight = bottomSheetHeightState,
                    onWordClick = onWordClick
                )
            }
        ) {

            val infoSectionBottomPadding = remember { mutableStateOf(0.dp) }

            LetterPracticeWritingInfoSection(
                state = infoSectionState,
                bottomSheetHeight = bottomSheetHeightState,
                onExpressionsClick = openBottomSheet,
                extraBottomPaddingState = infoSectionBottomPadding,
                speakKana = speakKana,
                modifier = Modifier.fillMaxSize(),
            )

            LetterPracticeWritingInputSection(
                state = reviewState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .trackItemPosition {
                        infoSectionBottomPadding.value = it.heightFromScreenBottom
                    }
                    .sizeIn(maxWidth = 400.dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
            )

            answersSection()

        }

    } else {

        val infoSection: @Composable RowScope.() -> Unit = {
            Material3BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetContent = {
                    LetterPracticeWritingWordsBottomSheet(
                        state = wordsBottomSheetState,
                        sheetContentHeight = bottomSheetHeightState,
                        onWordClick = onWordClick
                    )
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                LetterPracticeWritingInfoSection(
                    state = infoSectionState,
                    bottomSheetHeight = bottomSheetHeightState,
                    onExpressionsClick = openBottomSheet,
                    speakKana = speakKana,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val inputSection: @Composable RowScope.() -> Unit = {
            LetterPracticeWritingInputSection(
                state = reviewState,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .wrapContentSize()
                    .sizeIn(maxWidth = 400.dp)
                    .aspectRatio(1f)
                    .padding(20.dp)
            )
        }

        val (firstSection, secondSection) = when (layoutConfiguration.leftHandedMode) {
            true -> inputSection to infoSection
            false -> infoSection to inputSection
        }

        Box {

            Row(
                modifier = Modifier.fillMaxSize()
            ) {

                firstSection()

                secondSection()

            }

            answersSection()

        }

    }

}


private sealed interface LetterWritingButtonsState {
    object Hidden : LetterWritingButtonsState
    object StudyButtons : LetterWritingButtonsState
    data class DefaultButtons(
        val answers: PracticeAnswers,
        val mistakes: Int
    ) : LetterWritingButtonsState
}

@Composable
private fun State<LetterPracticeReviewState.Writing>.toAnswerButtonsState(): State<LetterWritingButtonsState> =
    remember {
        derivedStateOf {
            val currentState = value
            val writerState = currentState.writerState.value
            val progress = writerState.progress.value

            when {
                progress is CharacterWritingProgress.Completed.Idle -> {
                    if (currentState.isStudyMode.value) LetterWritingButtonsState.StudyButtons
                    else LetterWritingButtonsState.DefaultButtons(
                        answers = currentState.answers,
                        mistakes = progress.mistakes
                    )
                }

                else -> LetterWritingButtonsState.Hidden
            }
        }
    }


@Composable
private fun AnswerButtons(
    letterWritingButtonsState: State<LetterWritingButtonsState>,
    studyCompleted: () -> Unit,
    answerSelected: (PracticeAnswer) -> Unit,
    modifier: Modifier
) {

    val buttonsTransition = updateTransition(letterWritingButtonsState.value)
    buttonsTransition.AnimatedContent(
        transitionSpec = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) + fadeIn() togetherWith
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) + fadeOut()
        },
        contentKey = { it !is LetterWritingButtonsState.Hidden },
        modifier = modifier
    ) { writingButtonsState ->

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {

            when (writingButtonsState) {
                LetterWritingButtonsState.Hidden -> {

                }

                LetterWritingButtonsState.StudyButtons -> {
                    PracticeAnswerButtonsContainer {
                        Button(
                            onClick = studyCompleted,
                            modifier = Modifier.width(400.dp)
                                .padding(horizontal = 20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface,
                                contentColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(text = resolveString { letterPractice.studyFinishedButton })
                        }
                    }
                }

                is LetterWritingButtonsState.DefaultButtons -> {
                    PracticeAnswerButtonsRow(
                        answers = writingButtonsState.answers,
                        onClick = {
                            val updatedAnswer = it.copy(mistakes = writingButtonsState.mistakes)
                            answerSelected(updatedAnswer)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WritingPracticeVocabHeadline(
    word: LetterPracticeExampleWord,
    reveal: Boolean,
    letter: String
) {
    when {
        word.romaji != null -> {
            Text(formattedVocabDefinition(word.romaji, word.word.combinedGlossary()))
        }

        reveal -> {
            FuriganaWordHeadline(
                reading = word.word.reading,
                glossary = word.word.combinedGlossary()
            )
        }

        else -> {
            val text = formattedVocabDefinition(word.word.reading, word.word.combinedGlossary())
                .withEncodedText(letter)
            FuriganaText(text)
        }
    }
}
