package  ua.syt0r.kanji.presentation.screen.main.screen.home

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import ua.syt0r.kanji.core.sync.SyncFeatureState
import ua.syt0r.kanji.core.sync.SyncManager
import ua.syt0r.kanji.core.sync.SyncState
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.core.user_data.preferences.PreferencesDefaultHomeTab

class HomeViewModel(
    viewModelScope: CoroutineScope,
    private val appPreferences: PreferencesContract.AppPreferences,
    private val syncManager: SyncManager
) : HomeScreenContract.ViewModel {

    override val defaultTab: HomeScreenTab = runBlocking {
        when (appPreferences.defaultHomeTab.get()) {
            PreferencesDefaultHomeTab.GeneralDashboard -> HomeScreenTab.GeneralDashboard
            PreferencesDefaultHomeTab.Letters -> HomeScreenTab.LettersDashboard
            PreferencesDefaultHomeTab.Vocab -> HomeScreenTab.VocabDashboard
        }
    }

    private data class SyncIconData(
        val isLoading: Boolean = false,
        val indicator: SyncIconIndicator = SyncIconIndicator.Disabled
    )

    private val syncLoading = mutableStateOf(false)
    private val syncIndicator = mutableStateOf(SyncIconIndicator.Disabled)

    override val syncIconState = SyncIconState(syncLoading, syncIndicator)

    init {

        syncManager.state
            .flatMapLatest { it.toIconDataFlow() }
            .onEach { indicatorData: SyncIconData ->
                syncLoading.value = indicatorData.isLoading
                syncIndicator.value = indicatorData.indicator
            }
            .launchIn(viewModelScope)

    }

    override fun trySync(): Boolean {
        when (val syncState = syncManager.state.value) {
            SyncFeatureState.Disabled,
            SyncFeatureState.Loading,
            is SyncFeatureState.Error -> return false

            is SyncFeatureState.Enabled -> {
                syncState.sync()
                return true
            }
        }
    }

    private fun SyncFeatureState.toIconDataFlow(): Flow<SyncIconData> {
        return when (this) {
            SyncFeatureState.Disabled -> flowOf(SyncIconData())
            SyncFeatureState.Loading -> flowOf(SyncIconData(true))
            is SyncFeatureState.Error -> flowOf(SyncIconData(false, SyncIconIndicator.Error))
            is SyncFeatureState.Enabled -> toIconDataFlow()
        }
    }

    private fun SyncFeatureState.Enabled.toIconDataFlow(): Flow<SyncIconData> {
        return state.flatMapLatest {
            when (it) {
                SyncState.Refreshing,
                SyncState.Uploading,
                SyncState.Downloading -> flowOf(SyncIconData(true))

                is SyncState.TrackingChanges -> it.uploadAvailable.map { uploadAvailable ->
                    SyncIconData(
                        isLoading = false,
                        indicator = when {
                            uploadAvailable -> SyncIconIndicator.PendingUpload
                            else -> SyncIconIndicator.UpToDate
                        }
                    )
                }

                SyncState.Canceled -> flowOf(SyncIconData(false, SyncIconIndicator.Canceled))
                is SyncState.Conflict -> flowOf(SyncIconData(false, SyncIconIndicator.Disabled))
                is SyncState.Error -> flowOf(SyncIconData(false, SyncIconIndicator.Error))
            }
        }
    }

}