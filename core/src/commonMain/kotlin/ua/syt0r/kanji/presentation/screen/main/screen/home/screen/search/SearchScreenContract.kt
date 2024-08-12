package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search

import androidx.compose.runtime.State
import ua.syt0r.kanji.presentation.common.PaginatableJapaneseWordList
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.RadicalSearchListItem
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.RadicalSearchState
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.search.data.SearchByRadicalsResult

interface SearchScreenContract {

    companion object {
        const val InitialWordsCount = 50
        const val LoadMoreWordsCount = 50
        const val LoadMoreWordsFromEndThreshold = 20
    }

    interface ViewModel {

        val state: State<ScreenState>
        val radicalsState: State<RadicalSearchState>

        fun search(input: String)
        fun loadMoreWords()

        // Added for performance issues, loaded list makes switching to screen junky
        fun loadRadicalsData()
        fun radicalsSearch(radicals: Set<String>)

    }

    data class ScreenState(
        val isLoading: Boolean,
        val characters: List<String>,
        val words: State<PaginatableJapaneseWordList>,
        val query: String
    )

    interface ProcessInputUseCase {
        suspend fun process(input: String): ScreenState
    }

    interface LoadRadicalsUseCase {
        suspend fun load(): List<RadicalSearchListItem>
    }

    interface SearchByRadicalsUseCase {
        suspend fun search(radicals: Set<String>): SearchByRadicalsResult
    }

    interface LoadMoreWordsUseCase {
        suspend fun loadMore(state: ScreenState)
    }

    interface UpdateEnabledRadicalsUseCase {
        suspend fun update(
            allRadicals: List<RadicalSearchListItem>,
            selectedRadicals: Set<String>
        ): List<RadicalSearchListItem>
    }

}