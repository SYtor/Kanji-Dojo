package ua.syt0r.kanji.presentation.screen.main.screen.kanji_info

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.KanjiInfoScreenContract.ScreenState

class KanjiInfoViewModel(
    private val viewModelScope: CoroutineScope,
    private val loadDataUseCase: KanjiInfoScreenContract.LoadDataUseCase,
    private val loadCharacterWordsUseCase: KanjiInfoScreenContract.LoadCharacterWordsUseCase,
    private val analyticsManager: AnalyticsManager
) : KanjiInfoScreenContract.ViewModel {

    override val state = mutableStateOf<ScreenState>(ScreenState.Loading)

    private val wordChannel = Channel<Int>(Channel.RENDEZVOUS, BufferOverflow.DROP_LATEST)

    init {
        handleLoadMoreWordsRequests()
    }

    override fun loadCharacterInfo(character: String) {
        val currentState = state.value
        if (currentState is ScreenState.Loaded) return

        viewModelScope.launch {
            state.value = withContext(Dispatchers.IO) {
                loadDataUseCase.load(character)
            }
        }
    }


    override fun loadMoreWords() {
        val currentState = state.value as ScreenState.Loaded
        wordChannel.trySend(currentState.words.value.items.size)
    }

    override fun reportCharacter(character: String) {
        analyticsManager.sendEvent("kanji_info_open") { put("character", character) }
    }

    private fun handleLoadMoreWordsRequests() = viewModelScope.launch {
        wordChannel.consumeAsFlow()
            .distinctUntilChanged()
            .collect {
                val currentState = state.value as ScreenState.Loaded
                val wordsState = currentState.words as MutableState
                val currentWordsState = wordsState.value

                wordsState.value = withContext(Dispatchers.IO) {
                    val additionalWords = loadCharacterWordsUseCase.load(
                        character = currentState.character,
                        offset = currentWordsState.items.size,
                        limit = KanjiInfoScreenContract.LoadMoreWordsAmount
                    )
                    val newList = currentWordsState.items + additionalWords
                    currentWordsState.copy(items = newList)
                }
            }
    }

}