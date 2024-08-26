package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.use_case.DefaultGetDeckPickerCategoriesUseCase
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.use_case.GetDeckPickerCategoriesUseCase

val deckPickerScreenModule = module {

    factory<GetDeckPickerCategoriesUseCase> {
        DefaultGetDeckPickerCategoriesUseCase(appDataRepository = get())
    }

    multiplatformViewModel<DeckPickerScreenContract.ViewModel> {
        DeckPickerViewModel(
            viewModelScope = it.component1(),
            getDeckPickerCategoriesUseCase = get()
        )
    }

}