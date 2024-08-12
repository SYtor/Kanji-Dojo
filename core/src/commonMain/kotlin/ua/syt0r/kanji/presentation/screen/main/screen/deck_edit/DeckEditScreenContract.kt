package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.SearchResult

interface DeckEditScreenContract {

    interface ViewModel {
        val state: StateFlow<ScreenState>
        fun initialize(configuration: DeckEditScreenConfiguration)
        fun searchCharacters(input: String)
        fun dismissSearchResult()
        fun toggleRemoval(item: DeckEditListItem)
        fun saveDeck()
        fun deleteDeck()
    }

    sealed interface ScreenState {

        object Loading : ScreenState

        sealed interface Loaded : ScreenState {
            val title: MutableState<String>
            val confirmExit: State<Boolean>
            fun getCurrentList(): List<DeckEditListItem>
        }

        interface LetterDeckEditing : Loaded {
            val searching: State<Boolean>
            val listState: State<List<LetterDeckEditListItem>>
            val lastSearchResult: State<SearchResult?>
            override fun getCurrentList(): List<LetterDeckEditListItem> {
                return listState.value
            }
        }

        interface VocabDeckEditing : Loaded {
            val list: List<VocabDeckEditListItem>
            override fun getCurrentList(): List<DeckEditListItem> = list
        }

        object SavingChanges : ScreenState
        object Deleting : ScreenState

        data class Completed(val wasDeleted: Boolean) : ScreenState

    }

}

