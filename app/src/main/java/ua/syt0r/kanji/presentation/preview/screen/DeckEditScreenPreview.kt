package ua.syt0r.kanji.presentation.preview.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import ua.syt0r.kanji.presentation.common.theme.AppTheme
import ua.syt0r.kanji.presentation.common.ui.kanji.PreviewKanji
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditItemAction
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenUI
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.LetterDeckEditListItem
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.MutableLetterDeckEditingState


@Composable
private fun BasePreview(
    configuration: DeckEditScreenConfiguration,
    state: ScreenState
) {
    AppTheme {
        DeckEditScreenUI(
            configuration = configuration,
            state = rememberUpdatedState(state),
            navigateBack = {},
            submitSearch = {},
            onCharacterInfoClick = {},
            toggleRemoval = {},
            saveChanges = {},
            deleteDeck = {},
            onCompleted = {}
        )
    }
}

@Preview
@Composable
private fun CreatePreview() {
    BasePreview(
        configuration = DeckEditScreenConfiguration.LetterDeck.CreateNew,
        state = randomLetterEditState()
    )
}

@Preview
@Composable
private fun EditPreview() {
    BasePreview(
        configuration = DeckEditScreenConfiguration.LetterDeck.Edit("", 1),
        state = randomLetterEditState()
    )
}

@Composable
private fun randomLetterEditState() = remember {
    MutableLetterDeckEditingState(
        title = mutableStateOf(""),
        confirmExit = mutableStateOf(false),
        searching = mutableStateOf(false),
        listState = mutableStateOf(
            value = (1..80).map {
                LetterDeckEditListItem(
                    PreviewKanji.randomKanji(),
                    DeckEditItemAction.Nothing,
                    mutableStateOf(DeckEditItemAction.Nothing)
                )
            }
        ),
        lastSearchResult = mutableStateOf(value = null)
    )
}
