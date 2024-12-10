package ua.syt0r.kanji.presentation.screen.main.screen.sync

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ua.syt0r.kanji.core.AccountManager
import ua.syt0r.kanji.core.AccountState
import ua.syt0r.kanji.core.SubscriptionInfo

import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract.AppPreferences
import ua.syt0r.kanji.presentation.screen.main.screen.sync.SyncScreenContract.ScreenState


class SyncScreenViewModel(
    coroutineScope: CoroutineScope,
    private val appPreferences: AppPreferences,
    private val accountManager: AccountManager
) : SyncScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    init {
        coroutineScope.launch { loadData() }
    }

    private suspend fun CoroutineScope.loadData() {

        accountManager.state
            .onEach {
                _state.value = when (it) {
                    AccountState.Loading -> ScreenState.Loading
                    AccountState.LoggedOut -> ScreenState.Guide(
                        isSignedIn = false
                    )

                    is AccountState.LoggedIn -> {
                        if (it.subscriptionInfo is SubscriptionInfo.Active) {
                            val autoSync = mutableStateOf(appPreferences.syncEnabled.get())
                            snapshotFlow { autoSync.value }
                                .drop(1)
                                .onEach { appPreferences.syncEnabled.set(it) }
                                .launchIn(this)

                            ScreenState.SyncEnabled(
                                autoSync = autoSync,
                                lastSyncData = "-"
                            )
                        } else {
                            ScreenState.Guide(
                                isSignedIn = true
                            )
                        }
                    }

                    is AccountState.Error -> ScreenState.AccountError
                }
            }
            .collect()

    }

}