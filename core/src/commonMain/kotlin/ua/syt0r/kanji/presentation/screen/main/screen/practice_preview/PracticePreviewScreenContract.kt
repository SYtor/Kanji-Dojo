package ua.syt0r.kanji.presentation.screen.main.screen.practice_preview

import androidx.compose.runtime.State
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.FilterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticeGroup
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticeGroupsCreationResult
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticePreviewItem
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticePreviewScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.SortOption

interface PracticePreviewScreenContract {

    interface ViewModel {

        val state: State<ScreenState>

        fun updateScreenData(practiceId: Long)
        fun updateConfiguration(configuration: PracticePreviewScreenConfiguration)

        fun toggleSelectionMode()
        fun toggleSelection(character: String)
        fun toggleSelection(group: PracticeGroup)
        fun selectAll()
        fun deselectAll()

        fun getPracticeConfiguration(practiceGroup: PracticeGroup): MainDestination.Practice
        fun getMultiselectPracticeConfiguration(): MainDestination.Practice

        fun reportScreenShown()

    }

    sealed interface ScreenState {

        object Loading : ScreenState

        sealed interface Loaded : ScreenState {

            val title: String
            val configuration: PracticePreviewScreenConfiguration
            val allItems: List<PracticePreviewItem>
            val sharePractice: String

            val isSelectionModeEnabled: Boolean
            val selectedItems: Set<Any>

            data class Items(
                override val title: String,
                override val configuration: PracticePreviewScreenConfiguration,
                override val allItems: List<PracticePreviewItem>,
                override val sharePractice: String,
                override val isSelectionModeEnabled: Boolean,
                override val selectedItems: Set<String>,
                val visibleItems: List<PracticePreviewItem>
            ) : Loaded

            data class Groups(
                override val title: String,
                override val configuration: PracticePreviewScreenConfiguration,
                override val allItems: List<PracticePreviewItem>,
                override val sharePractice: String,
                override val isSelectionModeEnabled: Boolean,
                override val selectedItems: Set<Int>,
                val kanaGroupsMode: Boolean,
                val groups: List<PracticeGroup>
            ) : Loaded

        }

    }


    interface FetchItemsUseCase {
        suspend fun fetch(practiceId: Long): List<PracticePreviewItem>
    }

    interface SortItemsUseCase {
        fun sort(
            items: List<PracticePreviewItem>,
            sortOption: SortOption,
            isDescending: Boolean
        ): List<PracticePreviewItem>
    }

    interface FilterItemsUseCase {
        fun filter(
            items: List<PracticePreviewItem>,
            practiceType: PracticeType,
            filterConfiguration: FilterConfiguration
        ): List<PracticePreviewItem>
    }

    interface CreatePracticeGroupsUseCase {
        fun create(
            items: List<PracticePreviewItem>,
            visibleItems: List<PracticePreviewItem>,
            type: PracticeType,
            probeKanaGroups: Boolean
        ): PracticeGroupsCreationResult
    }

    interface ReloadDataUseCase {
        suspend fun load(practiceId: Long, previousState: ScreenState.Loaded?): ScreenState.Loaded
    }

}
