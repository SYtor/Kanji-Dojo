package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker

import androidx.compose.runtime.State
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker.data.LetterDeckPickerCategory

interface LetterDeckPickerScreenContract {

    interface ViewModel {
        val state: State<ScreenState>
        fun reportScreenShown()
    }

    sealed class ScreenState {

        object Loading : ScreenState()

        data class Loaded(
            val categories: List<LetterDeckPickerCategory>
        ) : ScreenState()

    }

}

