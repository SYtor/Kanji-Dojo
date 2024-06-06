package ua.syt0r.kanji.presentation.screen.main.screen.practice_preview

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.analytics.AnalyticsManager
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.PracticePreviewScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticeGroup
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticePreviewLayout
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticePreviewScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.practice_preview.data.PracticeType

class PracticePreviewViewModel(
    private val viewModelScope: CoroutineScope,
    private val reloadDataUseCase: PracticePreviewScreenContract.ReloadDataUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val filterItemsUseCase: PracticePreviewScreenContract.FilterItemsUseCase,
    private val sortItemsUseCase: PracticePreviewScreenContract.SortItemsUseCase,
    private val createGroupsUseCase: PracticePreviewScreenContract.CreatePracticeGroupsUseCase,
    private val analyticsManager: AnalyticsManager
) : PracticePreviewScreenContract.ViewModel {

    override val state = mutableStateOf<ScreenState>(ScreenState.Loading)

    private var practiceId: Long = -1

    override fun updateScreenData(practiceId: Long) {
        this.practiceId = practiceId
        val previousLoadedState = state.value as? ScreenState.Loaded

        viewModelScope.launch {
            state.value = ScreenState.Loading
            state.value = withContext(Dispatchers.IO) {
                reloadDataUseCase.load(practiceId, previousLoadedState)
            }
        }
    }

    override fun updateConfiguration(configuration: PracticePreviewScreenConfiguration) {
        val currentState = state.value
        if (currentState !is ScreenState.Loaded || currentState.configuration == configuration) {
            return
        }

        viewModelScope.launch {
            state.value = ScreenState.Loading

            state.value = withContext(Dispatchers.IO) {

                userPreferencesRepository.apply {
                    practiceType.set(configuration.practiceType.correspondingRepoType)
                    filterNew.set(configuration.filterConfiguration.showNew)
                    filterDue.set(configuration.filterConfiguration.showDue)
                    filterDone.set(configuration.filterConfiguration.showDone)
                    sortOption.set(configuration.sortOption.correspondingRepoType)
                    isSortDescending.set(configuration.isDescending)
                    practicePreviewLayout.set(configuration.layout.correspondingRepoType)
                    kanaGroupsEnabled.set(configuration.kanaGroups)
                }

                val visibleItems = filterItemsUseCase
                    .filter(
                        items = currentState.allItems,
                        practiceType = configuration.practiceType,
                        filterConfiguration = configuration.filterConfiguration
                    )
                    .let {
                        sortItemsUseCase.sort(
                            items = it,
                            sortOption = configuration.sortOption,
                            isDescending = configuration.isDescending
                        )
                    }

                when (configuration.layout) {
                    PracticePreviewLayout.SingleCharacter -> {
                        val selectedItems: Set<String> = when (currentState) {
                            is ScreenState.Loaded.Items -> visibleItems.map { it.character }.toSet()
                                .intersect(currentState.selectedItems)

                            else -> emptySet()
                        }

                        ScreenState.Loaded.Items(
                            title = currentState.title,
                            configuration = configuration,
                            allItems = currentState.allItems,
                            sharePractice = currentState.sharePractice,
                            isSelectionModeEnabled = currentState.isSelectionModeEnabled,
                            selectedItems = selectedItems,
                            visibleItems = visibleItems
                        )
                    }

                    PracticePreviewLayout.Groups -> {
                        val groupsCreationResult = createGroupsUseCase.create(
                            items = currentState.allItems,
                            visibleItems = visibleItems,
                            type = configuration.practiceType,
                            probeKanaGroups = configuration.kanaGroups
                        )

                        ScreenState.Loaded.Groups(
                            title = currentState.title,
                            configuration = configuration,
                            allItems = currentState.allItems,
                            sharePractice = currentState.sharePractice,
                            isSelectionModeEnabled = currentState.isSelectionModeEnabled,
                            selectedItems = currentState.let { it as? ScreenState.Loaded.Groups }
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
        }

    }

    override fun toggleSelectionMode() {
        val currentState = state.value as ScreenState.Loaded
        state.value = when (currentState) {
            is ScreenState.Loaded.Items -> {
                currentState.copy(
                    isSelectionModeEnabled = !currentState.isSelectionModeEnabled,
                    selectedItems = emptySet()
                )
            }

            is ScreenState.Loaded.Groups -> {
                currentState.copy(
                    isSelectionModeEnabled = !currentState.isSelectionModeEnabled,
                    selectedItems = emptySet()
                )
            }
        }
    }

    override fun toggleSelection(character: String) {
        val currentState = state.value as ScreenState.Loaded.Items
        state.value = currentState.run {
            copy(
                selectedItems = if (selectedItems.contains(character)) {
                    selectedItems.minus(character)
                } else {
                    selectedItems.plus(character)
                }
            )
        }
    }

    override fun toggleSelection(group: PracticeGroup) {
        val currentState = state.value as ScreenState.Loaded.Groups
        val index = group.index
        state.value = currentState.run {
            copy(
                selectedItems = if (selectedItems.contains(index)) {
                    selectedItems.minus(index)
                } else {
                    selectedItems.plus(index)
                }
            )
        }
    }

    override fun selectAll() {
        val currentState = state.value as ScreenState.Loaded
        state.value = when (currentState) {
            is ScreenState.Loaded.Items -> currentState.copy(
                selectedItems = currentState.visibleItems.map { it.character }.toSet()
            )

            is ScreenState.Loaded.Groups -> currentState.copy(
                selectedItems = currentState.groups.map { it.index }.toSet()
            )
        }
    }

    override fun deselectAll() {
        val currentState = state.value as ScreenState.Loaded
        state.value = when (currentState) {
            is ScreenState.Loaded.Items -> currentState.copy(
                selectedItems = emptySet()
            )

            is ScreenState.Loaded.Groups -> currentState.copy(
                selectedItems = emptySet()
            )
        }
    }

    override fun getPracticeConfiguration(practiceGroup: PracticeGroup): MainDestination.Practice {
        val currentState = state.value as ScreenState.Loaded
        val characters = practiceGroup.items.map { it.character }

        return when (currentState.configuration.practiceType) {
            PracticeType.Writing -> MainDestination.Practice.Writing(
                practiceId = practiceId,
                characterList = characters
            )

            PracticeType.Reading -> MainDestination.Practice.Reading(
                practiceId = practiceId,
                characterList = characters
            )
        }
    }

    override fun getMultiselectPracticeConfiguration(): MainDestination.Practice {
        val currentState = state.value.let { it as ScreenState.Loaded }

        val characters: List<String> = when (currentState) {
            is ScreenState.Loaded.Items -> currentState.run {
                allItems.map { it.character }.filter { selectedItems.contains(it) }
            }

            is ScreenState.Loaded.Groups -> currentState.run {
                groups.filter { selectedItems.contains(it.index) }
                    .flatMap { it.items.map { it.character } }
                    .toList()
            }
        }

        return when (currentState.configuration.practiceType) {
            PracticeType.Writing -> MainDestination.Practice.Writing(
                practiceId = practiceId,
                characterList = characters
            )

            PracticeType.Reading -> MainDestination.Practice.Reading(
                practiceId = practiceId,
                characterList = characters
            )
        }
    }

    override fun reportScreenShown() {
        analyticsManager.setScreen("practice_preview")
    }

}