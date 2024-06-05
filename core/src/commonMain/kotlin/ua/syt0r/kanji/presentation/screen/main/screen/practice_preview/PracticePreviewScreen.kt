package ua.syt0r.kanji.presentation.screen.main.screen.practice_preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import ua.syt0r.kanji.presentation.getMultiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration.LetterDeck
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.ui.PracticePreviewScreenUI

@Composable
fun PracticePreviewScreen(
    practiceId: Long,
    mainNavigationState: MainNavigationState,
    viewModel: PracticePreviewScreenContract.ViewModel = getMultiplatformViewModel()
) {

    val shouldInvalidateData = rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (shouldInvalidateData.value) {
            viewModel.updateScreenData(practiceId)
            shouldInvalidateData.value = false
        }
        viewModel.reportScreenShown()
    }

    PracticePreviewScreenUI(
        state = viewModel.state,
        onConfigurationUpdated = { viewModel.updateConfiguration(it) },
        onUpButtonClick = { mainNavigationState.navigateBack() },
        onEditButtonClick = {
            shouldInvalidateData.value = true
            val configuration = MainDestination.DeckEdit(
                LetterDeck.Edit(
                    title = "",
                    letterDeckId = practiceId
                )
            )
            mainNavigationState.navigate(configuration)
        },
        selectAllClick = { viewModel.selectAll() },
        deselectAllClick = { viewModel.deselectAll() },
        onCharacterClick = { mainNavigationState.navigate(MainDestination.KanjiInfo(it)) },
        onStartPracticeClick = { group ->
            shouldInvalidateData.value = true
            val configuration = viewModel.getPracticeConfiguration(group)
            mainNavigationState.navigate(configuration)
        },
        onDismissSelectionModeClick = { viewModel.toggleSelectionMode() },
        onEnableMultiselectClick = { viewModel.toggleSelectionMode() },
        onCharacterSelectionToggled = { viewModel.toggleSelection(it) },
        onGroupClickInMultiselectMode = { viewModel.toggleSelection(it) },
        onMultiselectPracticeStart = {
            shouldInvalidateData.value = true
            val configuration = viewModel.getPracticeConfiguration()
            mainNavigationState.navigate(configuration)
        }
    )

}
