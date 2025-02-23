package ua.syt0r.kanji.presentation.screen.main.screen.info

import androidx.compose.runtime.State
import kotlinx.coroutines.CoroutineScope

interface InfoScreenContract {

    companion object {
        const val VocabListPageItemsCount: Int = 50
        const val VocabListPrefetchDistance: Int = 50
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

            data class Vocab(
                val data: VocabInfoData
            ) : Loaded

        }

    }

    interface LoadLetterStateUseCase {
        suspend operator fun invoke(character: String, coroutineScope: CoroutineScope): ScreenState
    }

    interface LoadVocabStateUseCase {
        suspend operator fun invoke(
            data: InfoScreenData.Vocab,
            coroutineScope: CoroutineScope
        ): ScreenState
    }

}