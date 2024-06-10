package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case

import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.FilterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.toScreenType

interface GetLetterDeckDetailsConfigurationUseCase {
    suspend operator fun invoke(): LetterDeckDetailsConfiguration
}

class DefaultGetLetterDeckDetailsConfigurationUseCase(
    private val repository: UserPreferencesRepository,
) : GetLetterDeckDetailsConfigurationUseCase {

    override suspend fun invoke(): LetterDeckDetailsConfiguration {
        return repository.run {
            LetterDeckDetailsConfiguration(
                practiceType = practiceType.get().toScreenType(),
                filterConfiguration = FilterConfiguration(
                    showNew = filterNew.get(),
                    showDue = filterDue.get(),
                    showDone = filterDone.get(),
                ),
                sortOption = sortOption.get().toScreenType(),
                isDescending = isSortDescending.get(),
                layout = practicePreviewLayout.get().toScreenType(),
                kanaGroups = kanaGroupsEnabled.get(),
            )
        }
    }

}