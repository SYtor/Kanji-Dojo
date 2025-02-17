package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit

import androidx.compose.runtime.MutableState
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.ResolvedVocabCard
import ua.syt0r.kanji.core.app_data.WordClassification
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.user_data.database.SavedVocabCard
import ua.syt0r.kanji.core.user_data.database.VocabCardData
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
            val title: String,
            val classification: CharacterClassification
        ) : LetterDeck

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
            val title: String,
            val classification: WordClassification
        ) : VocabDeck

        @Serializable
        data class Edit(
            override val title: String,
            val vocabDeckId: Long
        ) : VocabDeck, EditExisting

    }

}

sealed interface DeckEditListItem {
    val initialAction: DeckEditItemAction
    val action: MutableState<DeckEditItemAction>
}

data class LetterDeckEditListItem(
    val character: String,
    override val initialAction: DeckEditItemAction,
    override val action: MutableState<DeckEditItemAction>
) : DeckEditListItem

sealed interface DeckEditVocabCard {

    val data: VocabCardData
    val resolvedCard: ResolvedVocabCard

    data class New(
        override val data: VocabCardData,
        override val resolvedCard: ResolvedVocabCard
    ) : DeckEditVocabCard

    data class Existing(
        val value: SavedVocabCard,
        override val resolvedCard: ResolvedVocabCard
    ) : DeckEditVocabCard {
        override val data: VocabCardData = value.data
    }

}

data class VocabDeckEditListItem(
    val card: DeckEditVocabCard,
    override val initialAction: DeckEditItemAction,
    override val action: MutableState<DeckEditItemAction>
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
    override val confirmExit: MutableState<Boolean>,
    override val list: List<VocabDeckEditListItem>
) : ScreenState.VocabDeckEditing
