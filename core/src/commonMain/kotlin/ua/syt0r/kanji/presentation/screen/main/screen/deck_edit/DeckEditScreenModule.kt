package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.DefaultDeleteDeckUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.DefaultLoadDeckEditLetterDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.DefaultLoadDeckEditVocabDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.DefaultSaveDeckUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.DefaultSearchValidCharactersUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.DeleteDeckUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.LoadDeckEditLetterDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.LoadDeckEditVocabDataUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.SaveDeckUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.SearchValidCharactersUseCase

val deckEditScreenModule = module {

    factory<LoadDeckEditLetterDataUseCase> {
        DefaultLoadDeckEditLetterDataUseCase(
            letterPracticeRepository = get(),
            appDataRepository = get()
        )
    }

    factory<LoadDeckEditVocabDataUseCase> {
        DefaultLoadDeckEditVocabDataUseCase(
            practiceRepository = get(),
            vocabCardResolver = get(),
            appDataRepository = get()
        )
    }

    factory<SearchValidCharactersUseCase> {
        DefaultSearchValidCharactersUseCase(
            appDataRepository = get()
        )
    }

    factory<SaveDeckUseCase> {
        DefaultSaveDeckUseCase(
            letterPracticeRepository = get(),
            vocabPracticeRepository = get()
        )
    }

    factory<DeleteDeckUseCase> {
        DefaultDeleteDeckUseCase(
            letterPracticeRepository = get(),
            vocabPracticeRepository = get()
        )
    }

    multiplatformViewModel<DeckEditScreenContract.ViewModel> {
        DeckEditViewModel(
            viewModelScope = it.component1(),
            loadDeckEditLetterDataUseCase = get(),
            loadDeckEditVocabDataUseCase = get(),
            searchValidCharactersUseCase = get(),
            saveDeckUseCase = get(),
            deleteDeckUseCase = get(),
            analyticsManager = get()
        )
    }

}