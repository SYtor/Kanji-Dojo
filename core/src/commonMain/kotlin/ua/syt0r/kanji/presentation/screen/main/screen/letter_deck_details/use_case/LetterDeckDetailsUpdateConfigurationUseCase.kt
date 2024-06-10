package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case

import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsConfiguration

interface UpdateLetterDeckDetailsConfigurationUseCase {
    suspend operator fun invoke(configuration: LetterDeckDetailsConfiguration)
}

class DefaultUpdateLetterDeckDetailsConfigurationUseCase(
    private val repository: UserPreferencesRepository,
) : UpdateLetterDeckDetailsConfigurationUseCase {

    override suspend fun invoke(configuration: LetterDeckDetailsConfiguration) {
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

}