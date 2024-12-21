package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.settings

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.presentation.screen.main.MainNavigationState


interface SettingsScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
    }

    sealed interface ScreenState {
        object Loading : ScreenState
        data class Loaded(val items: List<ListItem>) : ScreenState
    }

    interface ListItem {

        @Composable
        fun content(mainNavigationState: MainNavigationState)

    }

    interface ConfigurableListItem : ListItem {
        suspend fun prepare(coroutineScope: CoroutineScope)
    }

}
