package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.use_case

import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LettersDashboardScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.letters_dashboard.LetterDecksMergeRequestData

class MergeLettersDecksUseCase(
    private val repository: LetterPracticeRepository
) : LettersDashboardScreenContract.MergeDecksUseCase {

    override suspend fun merge(data: LetterDecksMergeRequestData) {
        repository.createPracticeAndMerge(data.title, data.deckIds)
    }

}