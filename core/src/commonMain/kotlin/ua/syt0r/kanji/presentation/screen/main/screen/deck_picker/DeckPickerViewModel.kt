package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.DeckPickerScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerLetterCategories
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerScreenConfiguration


class DeckPickerViewModel : DeckPickerScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    private lateinit var configuration: DeckPickerScreenConfiguration

    override fun loadData(configuration: DeckPickerScreenConfiguration) {
        if (::configuration.isInitialized) return

        this.configuration = configuration
        _state.value = when (configuration) {
            DeckPickerScreenConfiguration.Letters -> ScreenState.Loaded(DeckPickerLetterCategories)
            DeckPickerScreenConfiguration.Vocab -> TODO()
        }
    }

}