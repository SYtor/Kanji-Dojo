package ua.syt0r.kanji.presentation.screen.main.screen.info

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenContract.ScreenState

class InfoScreenViewModel(
    private val viewModelScope: CoroutineScope,
    private val screenData: InfoScreenData,
    private val loadDataUseCase: InfoScreenContract.LoadDataUseCase,
    private val analyticsManager: AnalyticsManager
) : InfoScreenContract.ViewModel {

    override val state = mutableStateOf<ScreenState>(ScreenState.Loading)

    init {
        loadInfo(screenData)
    }

    private fun loadInfo(screenData: InfoScreenData) {
        viewModelScope.launch {
            state.value = withContext(Dispatchers.IO) {
                when (screenData) {
                    is InfoScreenData.Letter -> {
                        reportLetter(screenData.letter)
                        loadDataUseCase.load(screenData.letter, viewModelScope)
                    }

                    is InfoScreenData.Vocab -> TODO()
                }
            }
        }
    }

    private fun reportLetter(letter: String) {
        analyticsManager.sendEvent("kanji_info_open") { put("character", letter) }
    }

}
