package ua.syt0r.kanji.presentation.screen.main.screen.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import ua.syt0r.kanji.core.AccountManager
import ua.syt0r.kanji.core.AccountState
import ua.syt0r.kanji.core.SubscriptionInfo
import ua.syt0r.kanji.core.toLocalDateTime
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

    private suspend fun loadData() {
        accountManager.state
            .onEach { accountState ->
                _state.value = when (accountState) {
                    AccountState.Loading -> ScreenState.Loading
                    AccountState.LoggedOut -> ScreenState.Guide(
                        isSignedIn = false
                    )

                    is AccountState.LoggedIn -> {
                        if (accountState.subscriptionInfo is SubscriptionInfo.Active) {
                            val syncDataInfo = appPreferences.lastSyncedDataInfo.get()
                            ScreenState.SyncEnabled(
                                lastSyncData = syncDataInfo?.dataTimestamp
                                    ?.let { Instant.fromEpochMilliseconds(it) }
                                    ?.toLocalDateTime()
                                    ?.format(LocalDateTime.Formats.ISO)
                                    ?: "-"
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