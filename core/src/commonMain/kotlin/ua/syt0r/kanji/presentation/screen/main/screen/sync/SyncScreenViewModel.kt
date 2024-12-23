package ua.syt0r.kanji.presentation.screen.main.screen.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import ua.syt0r.kanji.core.AccountManager
import ua.syt0r.kanji.core.AccountState
import ua.syt0r.kanji.core.sync.SyncFeatureState
import ua.syt0r.kanji.core.sync.SyncManager
import ua.syt0r.kanji.core.sync.use_case.GetLocalSyncDataInfoUseCase
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.screen.main.screen.sync.SyncScreenContract.ScreenState


class SyncScreenViewModel(
    coroutineScope: CoroutineScope,
    private val accountManager: AccountManager,
    private val syncManager: SyncManager,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val getLocalSyncDataInfoUseCase: GetLocalSyncDataInfoUseCase
) : SyncScreenContract.ViewModel {

    private val _state = MutableStateFlow<ScreenState>(ScreenState.Loading)
    override val state: StateFlow<ScreenState> = _state

    init {
        screenStateFlow()
            .onEach { _state.value = it }
            .launchIn(coroutineScope)
    }

    override fun sync() {
        when (val syncState = syncManager.state.value) {
            is SyncFeatureState.Enabled -> syncState.sync()
            else -> error("Unexpected state")
        }
    }

    private fun screenStateFlow(): Flow<ScreenState> {
        return syncManager.state.flatMapLatest {
            when (it) {
                SyncFeatureState.Loading -> flowOf(ScreenState.Loading)
                SyncFeatureState.Disabled,
                is SyncFeatureState.Error -> {
                    accountManager.state.flatMapLatest { accountState ->
                        when (accountState) {
                            AccountState.LoggedOut -> flowOf(ScreenState.Guide(false))
                            is AccountState.Error -> flowOf(ScreenState.AccountError)
                            AccountState.Loading -> flowOf(ScreenState.Loading)
                            is AccountState.LoggedIn -> flowOf(ScreenState.Guide(true))
                        }
                    }
                }

                is SyncFeatureState.Enabled -> flow {

                    coroutineScope {

                        val localSyncDataInfoFlow = merge(
                            appPreferences.localDataId.onModified,
                            appPreferences.localDataTimestamp.onModified
                        )
                            .map { Unit }
                            .onStart { emit(Unit) }
                            .map { getLocalSyncDataInfoUseCase() }
                            .stateIn(this)

                        val screenState = ScreenState.SyncEnabled(
                            syncState = it.state,
                            localDataState = localSyncDataInfoFlow
                        )

                        emit(screenState)

                    }

                }

            }
        }
    }

}