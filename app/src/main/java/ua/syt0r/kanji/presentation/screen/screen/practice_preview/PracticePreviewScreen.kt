package ua.syt0r.kanji.presentation.screen.screen.practice_preview

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import ua.syt0r.kanji.presentation.screen.MainContract
import ua.syt0r.kanji.presentation.screen.screen.practice_create.data.CreatePracticeConfiguration
import ua.syt0r.kanji.presentation.screen.screen.practice_preview.PracticePreviewScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.screen.practice_preview.data.SelectionOption
import ua.syt0r.kanji.presentation.screen.screen.practice_preview.ui.PracticePreviewScreenUI

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WritingPracticePreviewScreen(
    practiceId: Long,
    practiceTitle: String,
    navigation: MainContract.Navigation,
    viewModel: PracticePreviewScreenContract.ViewModel = hiltViewModel<PracticePreviewViewModel>(),
) {

    val screenState = viewModel.state.value

    LaunchedEffect(Unit) {
        viewModel.loadPracticeInfo(practiceId)
    }

    PracticePreviewScreenUI(
        title = practiceTitle,
        screenState = screenState,
        onUpButtonClick = { navigation.navigateBack() },
        onEditButtonClick = {
            val configuration = CreatePracticeConfiguration.EditExisting(
                practiceId = practiceId
            )
            navigation.navigateToPracticeCreate(configuration)
        },
        onSortSelected = {},
        onPracticeModeSelected = {
            screenState as ScreenState.Loaded
            viewModel.submitSelectionConfig(
                configuration = screenState.selectionConfig.copy(
                    practiceMode = it
                )
            )
        },
        onShuffleSelected = {
            screenState as ScreenState.Loaded
            viewModel.submitSelectionConfig(
                configuration = screenState.selectionConfig.copy(
                    shuffle = it
                )
            )
        },
        onSelectionOptionSelected = {
            screenState as ScreenState.Loaded
            viewModel.submitSelectionConfig(
                configuration = screenState.selectionConfig.copy(
                    option = it
                )
            )
        },
        onSelectionCountInputChanged = {
            screenState as ScreenState.Loaded
            viewModel.submitSelectionConfig(
                configuration = screenState.selectionConfig.copy(
                    firstItemsText = it
                )
            )
        },
        startPractice = {
            screenState as ScreenState.Loaded
            navigation.navigateToWritingPractice(
                viewModel.getPracticeConfiguration()
            )
        },
        onCharacterClick = {
            screenState as ScreenState.Loaded
            if (screenState.selectionConfig.option == SelectionOption.ManualSelection) {
                viewModel.toggleSelection(it)
            } else {
                navigation.navigateToKanjiInfo(it.character)
            }
        },
        onCharacterLongClick = {
            viewModel.toggleSelection(it)
        }
    )

}