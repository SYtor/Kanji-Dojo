package ua.syt0r.kanji.presentation.screen.main.screen.practice_create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_create.PracticeCreateScreenContract.ViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.practice_create.ui.PracticeCreateScreenUI

@Composable
fun PracticeCreateScreen(
    configuration: MainDestination.CreatePractice,
    mainNavigationState: MainNavigationState,
    viewModel: ViewModel
) {

    LaunchedEffect(Unit) {
        viewModel.initialize(configuration)
        viewModel.reportScreenShown(configuration)
    }

    PracticeCreateScreenUI(
        configuration = configuration,
        state = viewModel.state,
        onUpClick = {
            mainNavigationState.navigateBack()
        },
        onPracticeDeleteClick = {
            viewModel.deletePractice()
        },
        onDeleteAnimationCompleted = {
            mainNavigationState.popUpToHome()
        },
        onCharacterInfoClick = {
            mainNavigationState.navigate(MainDestination.KanjiInfo(it))
        },
        onCharacterDeleteClick = {
            viewModel.remove(it)
        },
        onCharacterRemovalCancel = {
            viewModel.cancelRemoval(it)
        },
        onSaveConfirmed = {
            viewModel.savePractice(it)
        },
        onSaveAnimationCompleted = {
            if (configuration is MainDestination.CreatePractice.EditExisting) {
                mainNavigationState.navigateBack()
            } else {
                mainNavigationState.popUpToHome()
            }
        },
        submitKanjiInput = {
            viewModel.submitUserInput(it)
        }
    )

}
