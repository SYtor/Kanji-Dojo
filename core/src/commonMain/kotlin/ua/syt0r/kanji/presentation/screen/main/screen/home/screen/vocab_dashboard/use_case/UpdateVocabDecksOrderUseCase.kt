package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case

import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.core.user_data.preferences.UserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksSortRequestData


interface UpdateVocabDecksOrderUseCase {
    suspend fun update(data: DecksSortRequestData)
}

class DefaultUpdateVocabDecksOrderUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val practiceRepository: VocabPracticeRepository
) : UpdateVocabDecksOrderUseCase {

    override suspend fun update(data: DecksSortRequestData) {
        userPreferencesRepository.dashboardSortByTime.set(data.sortByTime)
        practiceRepository.updateDeckPositions(
            deckIdToPositionMap = data.reorderedList.reversed()
                .mapIndexed { index, item -> item.deckId to index }
                .toMap()
        )
    }

}