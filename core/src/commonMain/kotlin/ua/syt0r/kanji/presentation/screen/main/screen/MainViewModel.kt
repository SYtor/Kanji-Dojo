package ua.syt0r.kanji.presentation.screen.main.screen

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ua.syt0r.kanji.BuildKonfig
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.MainContract

class MainViewModel(
    private val viewModelScope: CoroutineScope,
    private val preferencesRepository: UserPreferencesRepository
) : MainContract.ViewModel {

    override val shouldShowVersionChangeDialog = mutableStateOf(false)

    init {
        viewModelScope.launch {
            val wasVersionChangeDialogShownForCurrentVersion =
                BuildKonfig.versionName == preferencesRepository.lastAppVersionWhenChangesDialogShown.get()
            if (!wasVersionChangeDialogShownForCurrentVersion) {
                shouldShowVersionChangeDialog.value = true
            }
        }
    }

    override fun markVersionChangeDialogShown() {
        shouldShowVersionChangeDialog.value = false
        viewModelScope.launch {
            preferencesRepository.lastAppVersionWhenChangesDialogShown.set(BuildKonfig.versionName)
        }
    }

}