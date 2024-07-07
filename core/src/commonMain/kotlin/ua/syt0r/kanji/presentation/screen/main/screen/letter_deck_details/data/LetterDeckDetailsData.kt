package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.LetterDeckDetailsContract

class MutableLetterDeckDetailsLoadedState(
    override val title: String,
    override val allItems: List<LetterDeckDetailsItemData>,
    override val sharePractice: String,
    val mutableVisibleDataState: MutableState<MutableDeckDetailsVisibleData>,
) : LetterDeckDetailsContract.ScreenState.Loaded {
    override val visibleDataState: State<DeckDetailsVisibleData> = derivedStateOf {
        mutableVisibleDataState.value.asImmutable
    }
}

sealed interface DeckDetailsVisibleData {

    val configuration: LetterDeckDetailsConfiguration
    val items: List<DeckDetailsListItem>
    val isSelectionModeEnabled: State<Boolean>

    interface Items : DeckDetailsVisibleData {
        override val items: List<DeckDetailsListItem.Letter>
    }

    interface Groups : DeckDetailsVisibleData {
        val kanaGroupsMode: Boolean
        override val items: List<DeckDetailsListItem.Group>
        val selectedItem: State<DeckDetailsListItem.Group?>
    }

}

sealed interface MutableDeckDetailsVisibleData {

    val items: List<DeckDetailsListItem>
    val isSelectionModeEnabled: MutableState<Boolean>

    val asImmutable: DeckDetailsVisibleData

    data class Items(
        override val configuration: LetterDeckDetailsConfiguration,
        override val isSelectionModeEnabled: MutableState<Boolean>,
        override val items: List<DeckDetailsListItem.Letter>,
    ) : DeckDetailsVisibleData.Items, MutableDeckDetailsVisibleData {
        override val asImmutable: DeckDetailsVisibleData.Items = this
    }

    data class Groups(
        override val configuration: LetterDeckDetailsConfiguration,
        override val isSelectionModeEnabled: MutableState<Boolean>,
        override val kanaGroupsMode: Boolean,
        override val items: List<DeckDetailsListItem.Group>,
        override val selectedItem: MutableState<DeckDetailsListItem.Group?>,
    ) : DeckDetailsVisibleData.Groups, MutableDeckDetailsVisibleData {
        override val asImmutable: DeckDetailsVisibleData.Groups = this
    }

}

data class LetterDeckDetailsItemData(
    val character: String,
    val positionInPractice: Int,
    val frequency: Int?,
    val writingSummary: PracticeItemSummary,
    val readingSummary: PracticeItemSummary,
)

data class PracticeItemSummary(
    val firstReviewDate: LocalDateTime?,
    val lastReviewDate: LocalDateTime?,
    val expectedReviewDate: LocalDate?,
    val lapses: Int,
    val repeats: Int,
    val state: CharacterReviewState,
)


data class DeckDetailsListItemKey(
    val value: Any
)

sealed interface DeckDetailsListItem {

    val key: DeckDetailsListItemKey
    val selected: State<Boolean>

    data class Letter(
        val item: LetterDeckDetailsItemData,
        override val selected: State<Boolean>,
    ) : DeckDetailsListItem {
        override val key: DeckDetailsListItemKey = DeckDetailsListItemKey(item.character)
    }

    data class Group(
        val index: Int,
        val items: List<LetterDeckDetailsItemData>,
        val summary: PracticeGroupSummary,
        val reviewState: CharacterReviewState,
        override val selected: State<Boolean>,
    ) : DeckDetailsListItem {
        override val key: DeckDetailsListItemKey = DeckDetailsListItemKey(index)
    }
}

data class PracticeGroupSummary(
    val firstReviewDate: LocalDateTime?,
    val lastReviewDate: LocalDateTime?,
    val state: CharacterReviewState,
)

enum class CharacterReviewState {
    New,
    Due,
    Done
}

fun SrsItemStatus.toReviewState(): CharacterReviewState = when (this) {
    SrsItemStatus.New -> CharacterReviewState.New
    SrsItemStatus.Done -> CharacterReviewState.Done
    SrsItemStatus.Review -> CharacterReviewState.Due
}
