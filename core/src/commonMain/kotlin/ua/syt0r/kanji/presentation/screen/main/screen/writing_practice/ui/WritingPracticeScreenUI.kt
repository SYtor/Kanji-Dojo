package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.MultiplatformBackHandler
import ua.syt0r.kanji.presentation.common.jsonSaver
import ua.syt0r.kanji.presentation.common.resources.string.resolveString
import ua.syt0r.kanji.presentation.common.trackItemPosition
import ua.syt0r.kanji.presentation.common.ui.LocalOrientation
import ua.syt0r.kanji.presentation.common.ui.Material3BottomSheetScaffold
import ua.syt0r.kanji.presentation.common.ui.Orientation
import ua.syt0r.kanji.presentation.dialog.AlternativeWordsDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationCharactersSelection
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationContainer
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationEnumSelector
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeConfigurationOption
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeLeaveConfirmationDialog
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSavedState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSavingResult
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeSavingState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeToolbar
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.PracticeToolbarState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_common.rememberPracticeConfigurationCharactersSelectionState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.ReviewUserAction
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeHintMode
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeInputMode
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingScreenLayoutConfiguration

@Composable
fun WritingPracticeScreenUI(
    state: State<ScreenState>,
    navigateBack: () -> Unit,
    navigateToWordFeedback: (JapaneseWord) -> Unit,
    onConfigured: (WritingScreenConfiguration) -> Unit,
    toggleRadicalsHighlight: () -> Unit,
    toggleAutoPlay: () -> Unit,
    speakKana: (KanaReading) -> Unit,
    onNextClick: (ReviewUserAction) -> Unit,
    onPracticeSaveClick: (PracticeSavingResult) -> Unit,
    onPracticeCompleteButtonClick: () -> Unit
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
            state.value.let { !(it is ScreenState.Configuring || it is ScreenState.Saved) }
        }
    }

    if (shouldShowLeaveConfirmationOnBackClick.value) {
        MultiplatformBackHandler { shouldShowLeaveConfirmationDialog = true }
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

    Scaffold(
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
                ScreenState.Loading -> {
                    LoadingState()
                }

                is ScreenState.Configuring -> {
                    ConfiguringState(
                        state = screenState,
                        onClick = onConfigured
                    )
                }

                is ScreenState.Review -> {
                    ReviewState(
                        configuration = screenState.layoutConfiguration,
                        reviewState = screenState.reviewState.collectAsState(),
                        onNextClick = onNextClick,
                        toggleRadicalsHighlight = toggleRadicalsHighlight,
                        toggleAutoPlay = toggleAutoPlay,
                        speakKana = speakKana,
                        onWordClick = { selectedWordForAlternativeDialog = it }
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
                        charactersReviewed = screenState.run {
                            goodCharacters.size + repeatCharacters.size
                        },
                        practiceDuration = screenState.practiceDuration,
                        accuracy = screenState.accuracy,
                        failedCharacters = screenState.repeatCharacters,
                        goodCharacters = screenState.goodCharacters,
                        onFinishClick = onPracticeCompleteButtonClick
                    )
                }
            }

        }

    }

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
                    is ScreenState.Configuring -> flowOf(PracticeToolbarState.Configuration)
                    is ScreenState.Review -> screenState.reviewState.map {
                        it.practiceProgress.run {
                            PracticeToolbarState.Review(
                                pending = pendingCount,
                                repeat = repeatCount,
                                completed = finishedCount
                            )
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

@Composable
private fun ConfiguringState(
    state: ScreenState.Configuring,
    onClick: (WritingScreenConfiguration) -> Unit
) {

    val strings = resolveString { writingPractice }

    val characterSelectionState = rememberPracticeConfigurationCharactersSelectionState(
        characters = state.characters,
        shuffle = true
    )

    var noTranslationLayout by remember {
        mutableStateOf(state.noTranslationsLayout)
    }

    var leftHandedMode by remember {
        mutableStateOf(state.leftHandedMode)
    }

    var kanaRomaji by remember {
        mutableStateOf(state.kanaRomaji)
    }

    var altStrokeEvaluatorEnabled by remember {
        mutableStateOf(state.altStrokeEvaluatorEnabled)
    }

    val selectedHintMode = remember { mutableStateOf(WritingPracticeHintMode.OnlyNew) }

    val selectedInputMode = remember { mutableStateOf(state.inputMode) }

    PracticeConfigurationContainer(
        onClick = {
            val configuration = WritingScreenConfiguration(
                characters = characterSelectionState.result,
                shuffle = characterSelectionState.selectedShuffle.value,
                hintMode = selectedHintMode.value,
                inputMode = selectedInputMode.value,
                useRomajiForKanaWords = kanaRomaji,
                noTranslationsLayout = noTranslationLayout,
                leftHandedMode = leftHandedMode,
                altStrokeEvaluatorEnabled = altStrokeEvaluatorEnabled,
            )
            onClick(configuration)
        }
    ) {

        PracticeConfigurationCharactersSelection(
            state = characterSelectionState
        )

        PracticeConfigurationEnumSelector(
            title = resolveString { writingPractice.hintStrokesTitle },
            subtitle = resolveString { writingPractice.hintStrokesMessage },
            values = WritingPracticeHintMode.values(),
            selected = selectedHintMode.value,
            onSelected = { selectedHintMode.value = it }
        )

        PracticeConfigurationEnumSelector(
            title = resolveString { writingPractice.inputModeTitle },
            subtitle = resolveString { writingPractice.inputModeMessage },
            values = WritingPracticeInputMode.values(),
            selected = selectedInputMode.value,
            onSelected = { selectedInputMode.value = it }
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

}

@Composable
private fun LoadingState() {

    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ReviewState(
    configuration: WritingScreenLayoutConfiguration,
    reviewState: State<WritingReviewState>,
    onNextClick: (ReviewUserAction) -> Unit,
    toggleRadicalsHighlight: () -> Unit,
    toggleAutoPlay: () -> Unit,
    speakKana: (KanaReading) -> Unit,
    onWordClick: (JapaneseWord) -> Unit
) {

    val infoSectionState = reviewState.asInfoSectionState(
        noTranslationsLayout = configuration.noTranslationsLayout,
        radicalsHighlight = configuration.radicalsHighlight,
        kanaSoundAutoPlay = configuration.kanaAutoPlay
    )
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
                toggleRadicalsHighlight = toggleRadicalsHighlight,
                extraBottomPaddingState = infoSectionBottomPadding,
                toggleAutoPlay = toggleAutoPlay,
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
                    toggleRadicalsHighlight = toggleRadicalsHighlight,
                    toggleAutoPlay = toggleAutoPlay,
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

        val (firstSection, secondSection) = when (configuration.leftHandedMode) {
            true -> inputSection to infoSection
            false -> infoSection to inputSection
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            firstSection()

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )

            secondSection()

        }

    }

}
