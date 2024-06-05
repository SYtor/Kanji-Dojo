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
        fun toggleRemoval(item: DeckEditListItem)
        fun saveDeck()
        fun deleteDeck()
        fun reportScreenShown()
    }

    sealed interface ScreenState {

        object Loading : ScreenState

        sealed interface Loaded : ScreenState {
            val title: MutableState<String>
            val confirmExit: State<Boolean>
        }

        interface LetterDeckEditing : Loaded {
            val searching: State<Boolean>
            val listState: State<List<LetterDeckEditListItem>>
            val lastSearchResult: State<SearchResult?>
        }

        interface VocabDeckEditing : Loaded

        object SavingChanges : ScreenState
        object Deleting : ScreenState
        object Completed : ScreenState

    }

}

