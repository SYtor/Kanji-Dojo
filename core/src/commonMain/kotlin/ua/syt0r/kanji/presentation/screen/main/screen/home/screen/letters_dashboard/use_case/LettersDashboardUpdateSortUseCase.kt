package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case

import ua.syt0r.kanji.core.user_data.database.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.PreferencesContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksSortRequestData
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract

class LettersDashboardUpdateSortUseCase(
    private val appPreferences: PreferencesContract.AppPreferences,
    private val practiceRepository: LetterPracticeRepository
) : LettersDashboardScreenContract.UpdateSortUseCase {

    override suspend fun update(data: DecksSortRequestData) {
        appPreferences.letterDashboardSortByTime.set(data.sortByTime)
        practiceRepository.updateDeckPositions(
            deckIdToPositionMap = data.reorderedList.reversed()
                .mapIndexed { index, item -> item.deckId to index }
                .toMap()
        )
    }

}