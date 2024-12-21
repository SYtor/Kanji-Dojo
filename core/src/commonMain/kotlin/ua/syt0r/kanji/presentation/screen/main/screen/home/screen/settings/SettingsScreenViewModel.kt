package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings.SettingsScreenContract.ScreenState

class SettingsScreenViewModel(
    coroutineScope: CoroutineScope,
    settingItems: List<SettingsScreenContract.ListItem>,
) : SettingsScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    init {
        coroutineScope.launch {

            settingItems.filterIsInstance<SettingsScreenContract.ConfigurableListItem>()
                .forEach { it.prepare(coroutineScope) }

            _state.value = ScreenState.Loaded(settingItems)

        }
    }

}

