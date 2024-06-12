package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case

import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LetterDecksReorderRequestData

class LettersDashboardUpdateSortUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val practiceRepository: LetterPracticeRepository
) : LettersDashboardScreenContract.UpdateSortUseCase {

    override suspend fun update(data: LetterDecksReorderRequestData) {
        userPreferencesRepository.dashboardSortByTime.set(data.sortByTime)
        practiceRepository.updatePracticePositions(
            practiceIdToPositionMap = data.reorderedList.reversed()
                .mapIndexed { index, item -> item.deckId to index }
                .toMap()
        )
    }

}