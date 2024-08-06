package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val deckPickerScreenModule = module {

    multiplatformViewModel<DeckPickerScreenContract.ViewModel> {
        DeckPickerViewModel(analyticsManager = get())
    }

}