package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType


@Serializable
sealed interface DeckDetailsScreenConfiguration {

    val deckId: Long

    @Serializable
    data class LetterDeck(override val deckId: Long) : DeckDetailsScreenConfiguration

    @Serializable
    data class VocabDeck(override val deckId: Long) : DeckDetailsScreenConfiguration

}

sealed interface DeckDetailsData {

    val deckTitle: String
    val items: List<DeckDetailsItemData>

    data class LetterDeckData(
        override val deckTitle: String,
        override val items: List<DeckDetailsItemData.LetterData>,
        val sharableDeckData: String,
    ) : DeckDetailsData

    data class VocabDeckData(
        override val deckTitle: String,
        override val items: List<DeckDetailsItemData.VocabData>
    ) : DeckDetailsData

}

sealed interface DeckDetailsVisibleData {

    val items: List<DeckDetailsListItem>

    data class Items(
        override val items: List<DeckDetailsListItem.Letter>
    ) : DeckDetailsVisibleData

    data class Groups(
        override val items: List<DeckDetailsListItem.Group>,
        val kanaGroupsMode: Boolean,
        val selectedItem: MutableState<DeckDetailsListItem.Group?>
    ) : DeckDetailsVisibleData

    data class Vocab(
        override val items: List<DeckDetailsListItem.Vocab>
    ) : DeckDetailsVisibleData

}

sealed interface DeckDetailsItemData {

    data class LetterData(
        val character: String,
        val positionInPractice: Int,
        val frequency: Int?,
        val writingSummary: PracticeItemSummary,
        val readingSummary: PracticeItemSummary,
    ) : DeckDetailsItemData

    data class VocabData(
        val word: JapaneseWord,
        val positionInPractice: Int,
        val srsStatus: Map<VocabPracticeType, SrsItemStatus>
    ) : DeckDetailsItemData

}

data class PracticeItemSummary(
    val firstReviewDate: LocalDateTime?,
    val lastReviewDate: LocalDateTime?,
    val expectedReviewDate: LocalDate?,
    val lapses: Int,
    val repeats: Int,
    val srsItemStatus: SrsItemStatus,
)


data class DeckDetailsListItemKey(
    val value: Any
)

sealed interface DeckDetailsListItem {

    val key: DeckDetailsListItemKey
    val selected: MutableState<Boolean>

    data class Letter(
        override val key: DeckDetailsListItemKey,
        val data: DeckDetailsItemData.LetterData,
        val initialSelectionState: Boolean
    ) : DeckDetailsListItem {
        override val selected: MutableState<Boolean> = mutableStateOf(initialSelectionState)
    }

    data class Group(
        val index: Int,
        override val key: DeckDetailsListItemKey,
        val items: List<DeckDetailsItemData.LetterData>,
        val summary: PracticeGroupSummary,
        val reviewState: SrsItemStatus,
        val initialSelectionState: Boolean
    ) : DeckDetailsListItem {
        override val selected: MutableState<Boolean> = mutableStateOf(initialSelectionState)
    }

    data class Vocab(
        override val key: DeckDetailsListItemKey,
        val word: JapaneseWord,
        val statusMap: Map<VocabPracticeType, SrsItemStatus>,
        val initialSelectionState: Boolean
    ) : DeckDetailsListItem {
        override val selected: MutableState<Boolean> = mutableStateOf(initialSelectionState)
    }

}

data class PracticeGroupSummary(
    val firstReviewDate: LocalDateTime?,
    val lastReviewDate: LocalDateTime?,
    val state: SrsItemStatus,
)