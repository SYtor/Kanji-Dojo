package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker

import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerCategory
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerScreenConfiguration

interface DeckPickerScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun loadData(configuration: DeckPickerScreenConfiguration)
        fun reportScreenShown()
    }

    sealed class ScreenState {

        object Loading : ScreenState()

        data class Loaded(
            val categories: List<DeckPickerCategory>
        ) : ScreenState()

    }

}

