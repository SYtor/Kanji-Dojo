package ua.syt0r.kanji.presentation.screen.main.screen.sync

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract.AppPreferences
import ua.syt0r.kanji.presentation.screen.main.screen.sync.SyncScreenContract.ScreenState


class SyncScreenViewModel(
    coroutineScope: CoroutineScope,
    private val appPreferences: AppPreferences
) : SyncScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    init {
        coroutineScope.launch { loadData() }
    }

    private suspend fun CoroutineScope.loadData() {

        val autoSync = mutableStateOf(appPreferences.syncEnabled.get())

        snapshotFlow { autoSync.value }
            .onEach { appPreferences.syncEnabled.set(it) }
            .launchIn(this)

        _state.value = ScreenState.Loaded(
            isSignedIn = appPreferences.idToken.get() != null,
            isSubscriptionActive = true,
            autoSync = autoSync
        )

    }

}