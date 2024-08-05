package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case

import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.dashboard_common.DecksMergeRequestData

interface MergeVocabDecksUseCase {
    suspend operator fun invoke(data: DecksMergeRequestData)
}

class DefaultMergeVocabDecksUseCase(
    private val repository: VocabPracticeRepository
) : MergeVocabDecksUseCase {

    override suspend operator fun invoke(data: DecksMergeRequestData) {
        repository.createDeckAndMerge(data.title, data.deckIds)
    }

}