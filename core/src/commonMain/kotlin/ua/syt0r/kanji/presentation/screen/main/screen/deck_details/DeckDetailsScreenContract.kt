package ua.syt0r.kanji.presentation.screen.main.screen.deck_details

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsListItem
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsVisibleData

interface DeckDetailsScreenContract {

    interface ViewModel {

        val state: StateFlow<ScreenState>

        fun loadData(configuration: DeckDetailsScreenConfiguration)

        fun getPracticeConfiguration(group: DeckDetailsListItem.Group): MainDestination.LetterPractice
        fun getMultiselectPracticeConfiguration(): MainDestination

    }

    sealed interface ScreenState {

        object Loading : ScreenState

        sealed interface Loaded : ScreenState {

            val title: String
            val items: List<DeckDetailsItemData>
            val configuration: MutableState<out DeckDetailsConfiguration>
            val isSelectionModeEnabled: MutableState<Boolean>
            val visibleDataState: State<DeckDetailsVisibleData>

            data class Letters(
                override val title: String,
                override val items: List<DeckDetailsItemData.LetterData>,
                override val configuration: MutableState<DeckDetailsConfiguration.LetterDeckConfiguration>,
                override val isSelectionModeEnabled: MutableState<Boolean>,
                override val visibleDataState: State<DeckDetailsVisibleData>,
                val sharableDeckData: String
            ) : Loaded

            data class Vocab(
                override val title: String,
                override val items: List<DeckDetailsItemData.VocabData>,
                override val configuration: MutableState<DeckDetailsConfiguration.VocabDeckConfiguration>,
                override val isSelectionModeEnabled: MutableState<Boolean>,
                override val visibleDataState: State<DeckDetailsVisibleData>
            ) : Loaded

        }

    }

}
