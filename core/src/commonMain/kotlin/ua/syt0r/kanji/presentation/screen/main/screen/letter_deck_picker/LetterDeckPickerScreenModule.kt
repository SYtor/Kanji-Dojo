package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker

import org.koin.dsl.module
import ua.syt0r.kanji.presentation.multiplatformViewModel

val letterDeckPickerScreenModule = module {

    multiplatformViewModel<LetterDeckPickerScreenContract.ViewModel> {
        LetterDeckPickerViewModel(analyticsManager = get())
    }

}