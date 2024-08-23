package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.trackItemPosition
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Material3BottomSheetScaffold
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeAnswer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeLayoutConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeReviewState

@OptIn(ExperimentalMaterialApi::class)
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

    if (LocalOrientation.current == Orientation.Portrait) {

        Material3BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetContent = {
                WritingPracticeWordsBottomSheet(
                    state = wordsBottomSheetState,
                    sheetContentHeight = bottomSheetHeightState,
                    onWordClick = onWordClick
                )
            }
        ) {

            val infoSectionBottomPadding = remember { mutableStateOf(0.dp) }

            WritingPracticeInfoSection(
                state = infoSectionState,
                bottomSheetHeight = bottomSheetHeightState,
                onExpressionsClick = openBottomSheet,
                extraBottomPaddingState = infoSectionBottomPadding,
                speakKana = speakKana,
                modifier = Modifier.fillMaxSize(),
            )

            WritingPracticeInputSection(
                state = reviewState,
                onNextClick = onNextClick,
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

        }

    } else {

        val infoSection: @Composable RowScope.() -> Unit = {
            Material3BottomSheetScaffold(
                scaffoldState = scaffoldState,
                sheetContent = {
                    WritingPracticeWordsBottomSheet(
                        state = wordsBottomSheetState,
                        sheetContentHeight = bottomSheetHeightState,
                        onWordClick = onWordClick
                    )
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                WritingPracticeInfoSection(
                    state = infoSectionState,
                    bottomSheetHeight = bottomSheetHeightState,
                    onExpressionsClick = openBottomSheet,
                    speakKana = speakKana,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val inputSection: @Composable RowScope.() -> Unit = {
            WritingPracticeInputSection(
                state = reviewState,
                onNextClick = onNextClick,
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

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            firstSection()

            VerticalDivider(color = MaterialTheme.colorScheme.outline)

            secondSection()

        }

    }
}