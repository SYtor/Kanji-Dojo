package ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.use_case

import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.PracticePreviewScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.PracticePreviewScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.FilterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticePreviewLayout
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticePreviewScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.SortOption
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.toScreenType

class PracticePreviewReloadStateUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val practiceRepository: LetterPracticeRepository,
    private val fetchItemsUseCase: PracticePreviewScreenContract.FetchItemsUseCase,
    private val filterItemsUseCase: PracticePreviewScreenContract.FilterItemsUseCase,
    private val sortItemsUseCase: PracticePreviewScreenContract.SortItemsUseCase,
    private val createGroupsUseCase: PracticePreviewScreenContract.CreatePracticeGroupsUseCase,
) : PracticePreviewScreenContract.ReloadDataUseCase {

    override suspend fun load(
        practiceId: Long,
        previousState: ScreenState.Loaded?
    ): ScreenState.Loaded {
        val configuration = previousState?.configuration ?: getRepositoryConfiguration()

        val items = fetchItemsUseCase.fetch(practiceId)
        val visibleItems = filterItemsUseCase
            .filter(items, configuration.practiceType, configuration.filterConfiguration)
            .let {
                sortItemsUseCase.sort(it, configuration.sortOption, configuration.isDescending)
            }

        val title = practiceRepository.getPracticeInfo(practiceId).name
        val isSelectionModeEnabled = previousState?.isSelectionModeEnabled ?: false

        val sharePractice = sortItemsUseCase.sort(items, SortOption.ADD_ORDER, false)
            .joinToString("") { it.character }

        return when (configuration.layout) {
            PracticePreviewLayout.SingleCharacter -> {
                ScreenState.Loaded.Items(
                    title = title,
                    configuration = configuration,
                    allItems = items,
                    sharePractice = sharePractice,
                    isSelectionModeEnabled = isSelectionModeEnabled,
                    selectedItems = previousState.let { it as? ScreenState.Loaded.Items }
                        ?.let {
                            it.selectedItems.intersect(other = items.map { it.character }.toSet())
                        }
                        ?: emptySet(),
                    visibleItems = visibleItems
                )
            }

            PracticePreviewLayout.Groups -> {
                val groupsCreationResult = createGroupsUseCase.create(
                    items = items,
                    visibleItems = visibleItems,
                    type = configuration.practiceType,
                    probeKanaGroups = configuration.kanaGroups
                )

                ScreenState.Loaded.Groups(
                    title = title,
                    configuration = configuration,
                    allItems = items,
                    sharePractice = sharePractice,
                    isSelectionModeEnabled = isSelectionModeEnabled,
                    selectedItems = previousState.let { it as? ScreenState.Loaded.Groups }
                        ?.let {
                            it.selectedItems.intersect(
                                other = groupsCreationResult.groups.map { it.index }.toSet()
                            )
                        }
                        ?: emptySet(),
                    kanaGroupsMode = groupsCreationResult.kanaGroups,
                    groups = groupsCreationResult.groups
                )
            }
        }
    }

    private suspend fun getRepositoryConfiguration(): PracticePreviewScreenConfiguration {
        return userPreferencesRepository.run {
            PracticePreviewScreenConfiguration(
                practiceType = practiceType.get().toScreenType(),
                filterConfiguration = FilterConfiguration(
                    showNew = filterNew.get(),
                    showDue = filterDue.get(),
                    showDone = filterDone.get()
                ),
                sortOption = sortOption.get().toScreenType(),
                isDescending = isSortDescending.get(),
                layout = practicePreviewLayout.get().toScreenType(),
                kanaGroups = kanaGroupsEnabled.get()
            )
        }
    }

}