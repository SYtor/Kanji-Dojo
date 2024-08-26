package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.DeckPickerScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data.DeckPickerScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.use_case.GetDeckPickerCategoriesUseCase


class DeckPickerViewModel(
    private val viewModelScope: CoroutineScope,
    private val getDeckPickerCategoriesUseCase: GetDeckPickerCategoriesUseCase
) : DeckPickerScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    private lateinit var configuration: DeckPickerScreenConfiguration

    override fun loadData(configuration: DeckPickerScreenConfiguration) {
        if (::configuration.isInitialized) return
        this.configuration = configuration

        viewModelScope.launch {
            _state.value = ScreenState.Loaded(
                categories = getDeckPickerCategoriesUseCase(configuration)
            )
        }
    }

}