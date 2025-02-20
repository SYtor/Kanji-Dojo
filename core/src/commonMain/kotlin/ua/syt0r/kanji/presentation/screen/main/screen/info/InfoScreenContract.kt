package ua.syt0r.kanji.presentation.screen.main.screen.info

import androidx.compose.runtime.State
import kotlinx.coroutines.CoroutineScope
import ua.syt0r.kanji.core.app_data.data.JapaneseWord

interface InfoScreenContract {

    companion object {
        const val VocabListInitialCount: Int = 50
        const val VocabListPageCount: Int = 50
        const val VocabListPrefetchDistance: Int = 20
    }

    interface ViewModel {
        val state: State<ScreenState>
    }

    sealed interface ScreenState {

        data object Loading : ScreenState

        data object NoData : ScreenState

        sealed interface Loaded : ScreenState {

            data class Letter(
                val data: LetterInfoData
            ) : Loaded

        }

    }

    interface LoadDataUseCase {
        suspend fun load(character: String, coroutineScope: CoroutineScope): ScreenState
    }

    interface LoadCharacterWordsUseCase {
        suspend fun load(character: String, offset: Int, limit: Int): List<JapaneseWord>
    }

}