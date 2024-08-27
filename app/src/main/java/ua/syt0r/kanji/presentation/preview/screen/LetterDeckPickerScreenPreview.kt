package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.DeckPickerScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.DeckPickerScreenUI
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerLetterCategories

@Preview
@Composable
private fun LoadingPreview() {
    ScreenPreview(screenState = ScreenState.Loading)
}

@Preview
@Composable
private fun LoadedPreview() {
    ScreenPreview(screenState = ScreenState.Loaded(DeckPickerLetterCategories))
}

@Composable
private fun ScreenPreview(
    screenState: ScreenState
) {
    AppTheme {
        DeckPickerScreenUI(
            state = rememberUpdatedState(screenState),
            onUpButtonClick = {},
            createEmpty = {},
            onLetterDeckClick = { _, _ -> },
            onVocabDeckClick = { _, _ -> },
            onLinkClick = {}
        )
    }
}
