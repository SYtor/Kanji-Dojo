package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker.LetterDeckPickerScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker.LetterDeckPickerScreenUI
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker.data.AllImportCategories

@Preview
@Composable
private fun LoadingPreview() {
    ScreenPreview(screenState = ScreenState.Loading)
}

@Preview
@Composable
private fun LoadedPreview() {
    ScreenPreview(screenState = ScreenState.Loaded(AllImportCategories))
}

@Composable
private fun ScreenPreview(
    screenState: ScreenState
) {
    AppTheme {
        LetterDeckPickerScreenUI(
            state = rememberUpdatedState(screenState),
            onUpButtonClick = {},
            createEmpty = {},
            onItemSelected = { _, _ -> },
            onLinkClick = {}
        )
    }
}
