package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case

import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsConfiguration

interface UpdateDeckDetailsConfigurationUseCase {

    suspend operator fun invoke(
        configuration: DeckDetailsConfiguration.LetterDeckConfiguration
    )

    suspend operator fun invoke(
        configuration: DeckDetailsConfiguration.VocabDeckConfiguration
    )

}

class DefaultUpdateDeckDetailsConfigurationUseCase(
    private val repository: UserPreferencesRepository,
) : UpdateDeckDetailsConfigurationUseCase {

    override suspend fun invoke(configuration: DeckDetailsConfiguration.LetterDeckConfiguration) {
        repository.apply {
            practiceType.set(configuration.practiceType.correspondingRepoType)
            filterNew.set(configuration.filterConfiguration.showNew)
            filterDue.set(configuration.filterConfiguration.showDue)
            filterDone.set(configuration.filterConfiguration.showDone)
            sortOption.set(configuration.sortOption.correspondingRepoType)
            isSortDescending.set(configuration.isDescending)
            practicePreviewLayout.set(configuration.layout.correspondingRepoType)
            kanaGroupsEnabled.set(configuration.kanaGroups)
        }
    }

    override suspend fun invoke(configuration: DeckDetailsConfiguration.VocabDeckConfiguration) {

    }

}