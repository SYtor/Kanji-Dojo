package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case

import androidx.compose.runtime.mutableStateOf
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsLayout
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsListItem
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsListItemKey
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsVisibleData

interface GetDeckDetailsVisibleDataUseCase {

    operator fun invoke(
        items: List<DeckDetailsItemData.LetterData>,
        configuration: DeckDetailsConfiguration.LetterDeckConfiguration,
        currentVisibleData: DeckDetailsVisibleData?
    ): DeckDetailsVisibleData

    operator fun invoke(
        items: List<DeckDetailsItemData.VocabData>,
        configuration: DeckDetailsConfiguration.VocabDeckConfiguration,
        currentVisibleData: DeckDetailsVisibleData?
    ): DeckDetailsVisibleData.Vocab

}

class DefaultGetDeckDetailsVisibleDataUseCase(
    private val applyFilterUseCase: DeckDetailsApplyFilterUseCase,
    private val applySortUseCase: DeckDetailsApplySortUseCase,
    private val createGroupsUseCase: DeckDetailsCreateLetterGroupsUseCase,
) : GetDeckDetailsVisibleDataUseCase {

    override operator fun invoke(
        items: List<DeckDetailsItemData.LetterData>,
        configuration: DeckDetailsConfiguration.LetterDeckConfiguration,
        currentVisibleData: DeckDetailsVisibleData?
    ): DeckDetailsVisibleData {

        val selectionStates = currentVisibleData?.items
            ?.associate { it.key to it.selected.value }

        val visibleItems = applyFilterUseCase(
            items = items,
            practiceType = configuration.practiceType,
            filterConfiguration = configuration.filterConfiguration
        )
            .let {
                applySortUseCase(
                    items = it,
                    sortOption = configuration.sortOption,
                    isDescending = configuration.isDescending
                )
            }

        return when (configuration.layout) {
            DeckDetailsLayout.SingleCharacter -> {

                DeckDetailsVisibleData.Items(
                    items = visibleItems.map { letterData ->
                        val key = DeckDetailsListItemKey(letterData.character)
                        DeckDetailsListItem.Letter(
                            key = key,
                            data = letterData,
                            initialSelectionState = selectionStates?.get(key) ?: false
                        )
                    },
                )
            }

            DeckDetailsLayout.Groups -> {
                val groupsCreationResult = createGroupsUseCase(
                    items = items,
                    visibleItems = visibleItems,
                    type = configuration.practiceType,
                    probeKanaGroups = configuration.kanaGroups,
                    previousSelectionStates = selectionStates
                )

                val currentSelectedGroup = currentVisibleData
                    ?.let { it as? DeckDetailsVisibleData.Groups }
                    ?.selectedItem
                    ?.value

                val updatedSelectedGroup = currentSelectedGroup?.index?.let { groupIndex ->
                    groupsCreationResult.groups.find { it.index == groupIndex }
                }

                DeckDetailsVisibleData.Groups(
                    kanaGroupsMode = groupsCreationResult.kanaGroups,
                    items = groupsCreationResult.groups,
                    selectedItem = mutableStateOf(updatedSelectedGroup)
                )
            }
        }
    }

    override fun invoke(
        items: List<DeckDetailsItemData.VocabData>,
        configuration: DeckDetailsConfiguration.VocabDeckConfiguration,
        currentVisibleData: DeckDetailsVisibleData?
    ): DeckDetailsVisibleData.Vocab {

        val selectionStates = currentVisibleData
            ?.let { it as? DeckDetailsVisibleData.Vocab }
            ?.items
            ?.associate { it.key to it.selected.value }

        val filteredItems = applyFilterUseCase(
            items = items,
            practiceType = configuration.practiceType,
            filterConfiguration = configuration.filterConfiguration
        )

        return DeckDetailsVisibleData.Vocab(
            items = filteredItems.map { vocabData ->
                val key = DeckDetailsListItemKey(vocabData.word.id.toString())
                DeckDetailsListItem.Vocab(
                    key = key,
                    word = vocabData.word,
                    statusMap = vocabData.srsStatus,
                    initialSelectionState = selectionStates?.get(key) ?: false
                )
            }
        )
    }

}