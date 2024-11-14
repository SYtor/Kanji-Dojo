package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case

import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.FilterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.toScreenType

interface DeckDetailsGetConfigurationUseCase {
    suspend fun lettersConfiguration(): DeckDetailsConfiguration.LetterDeckConfiguration
    suspend fun vocabConfiguration(): DeckDetailsConfiguration.VocabDeckConfiguration
}

class DefaultDeckDetailsGetConfigurationUseCase(
    private val repository: PreferencesContract.AppPreferences,
) : DeckDetailsGetConfigurationUseCase {

    override suspend fun lettersConfiguration(): DeckDetailsConfiguration.LetterDeckConfiguration {
        return repository.run {
            DeckDetailsConfiguration.LetterDeckConfiguration(
                practiceType = ScreenLetterPracticeType.from(practiceType.get()),
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

    override suspend fun vocabConfiguration(): DeckDetailsConfiguration.VocabDeckConfiguration {
        return repository.run {
            DeckDetailsConfiguration.VocabDeckConfiguration(
                practiceType = ScreenVocabPracticeType.Flashcard,
                filterConfiguration = FilterConfiguration(
                    showNew = true,
                    showDue = true,
                    showDone = true
                )
            )
        }
    }

}