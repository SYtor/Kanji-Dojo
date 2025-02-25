package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.app_data.VocabSenseGroup
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
        data object CreateNew : LetterDeck

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
        data object CreateNew : VocabDeck

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

data class VocabDeckEditListItem(
    val cardData: VocabCardData,
    val savedVocabCard: SavedVocabCard?,
    val dictionarySenseList: List<VocabSenseGroup.Sense>,
    override val initialAction: DeckEditItemAction
) : DeckEditListItem {

    override val action: MutableState<DeckEditItemAction> = mutableStateOf(initialAction)
    val modifiedData: MutableState<VocabCardData?> = mutableStateOf(null)

    val displayCardData: State<VocabCardData> = derivedStateOf { modifiedData.value ?: cardData }
    val displayMeaning: State<String> = derivedStateOf {
        val cardData = modifiedData.value ?: cardData
        cardData.meaning ?: getDictionaryMeaning(cardData.kanjiReading, cardData.kanaReading)
    }

    fun getDictionaryMeaning(kanjiReading: String?, kanaReading: String): String {
        val matchingSense = dictionarySenseList.first {
            val kanjiCheck = it.kanjiRestrictions.isEmpty() ||
                    it.kanjiRestrictions.contains(kanjiReading)
            val kanaCheck = it.kanaRestrictions.isEmpty() ||
                    it.kanaRestrictions.contains(kanaReading)
            kanjiCheck && kanaCheck
        }
        return matchingSense.glossary.joinToString()
    }

}

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
