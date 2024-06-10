package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details

import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsListItem
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.DeckDetailsVisibleData
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsItemData

interface LetterDeckDetailsContract {

    interface ViewModel {

        val state: StateFlow<ScreenState>

        fun notifyScreenShown(practiceId: Long)
        fun updateConfiguration(configuration: LetterDeckDetailsConfiguration)

        fun showGroupDetails(group: DeckDetailsListItem.Group)
        fun toggleSelectionMode()
        fun toggleSelection(item: DeckDetailsListItem)
        fun selectAll()
        fun deselectAll()

        fun getPracticeConfiguration(group: DeckDetailsListItem.Group): MainDestination.Practice
        fun getMultiselectPracticeConfiguration(): MainDestination.Practice

        fun reportScreenShown()

    }

    sealed interface ScreenState {

        object Loading : ScreenState

        interface Loaded : ScreenState {
            val title: String
            val allItems: List<LetterDeckDetailsItemData>
            val sharePractice: String
            val visibleDataState: State<DeckDetailsVisibleData>
        }

    }

}
