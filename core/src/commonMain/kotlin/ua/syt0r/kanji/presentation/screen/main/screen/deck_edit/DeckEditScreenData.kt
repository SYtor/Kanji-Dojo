package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case.SearchResult

@Serializable
sealed interface DeckEditScreenConfiguration {

    interface EditExisting {
        val title: String
    }

    @Serializable
    sealed interface LetterDeck : DeckEditScreenConfiguration {

        @Serializable
        object CreateNew : LetterDeck

        @Serializable
        data class CreateDerived(
            override val title: String,
            val classification: CharacterClassification
        ) : LetterDeck, EditExisting

        @Serializable
        data class Edit(
            override val title: String,
            val letterDeckId: Long
        ) : LetterDeck, EditExisting

    }

    @Serializable
    sealed interface VocabDeck : DeckEditScreenConfiguration {

        @Serializable
        object CreateNew : VocabDeck

        @Serializable
        data class CreateDerived(
            override val title: String,
            val words: List<Long>
        ) : VocabDeck, EditExisting

        @Serializable
        data class Edit(
            override val title: String,
            val vocabDeckId: Long
        ) : VocabDeck, EditExisting

    }

}

enum class DeckEditingMode(
    val icon: ImageVector
) {
    Search(Icons.Default.Search),
    Removal(Icons.Default.Close),
    ResetSrs(Icons.Default.Memory)
}

sealed interface DeckEditListItem {
    val initialAction: DeckEditItemAction
}

data class LetterDeckEditListItem(
    val character: String,
    override val initialAction: DeckEditItemAction,
    val action: State<DeckEditItemAction>
) : DeckEditListItem

data class VocabDeckEditListItem(
    val word: JapaneseWord,
    override val initialAction: DeckEditItemAction,
    val action: State<DeckEditItemAction>
) : DeckEditListItem

enum class DeckEditItemAction { Nothing, Add, Remove }


data class MutableLetterDeckEditingState(
    override val title: MutableState<String>,
    override val confirmExit: MutableState<Boolean>,
    override val searching: MutableState<Boolean>,
    override val listState: MutableState<List<LetterDeckEditListItem>>,
    override val lastSearchResult: MutableState<SearchResult?>
) : ScreenState.LetterDeckEditing

data class MutableVocabDeckEditingState(
    override val title: MutableState<String>,
    override val confirmExit: MutableState<Boolean>
) : ScreenState.VocabDeckEditing
