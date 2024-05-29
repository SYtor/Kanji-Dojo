package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case

import ua.syt0r.kanji.core.user_data.PracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.VocabPracticeDeck
import ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.vocabDecks

interface GetVocabDecksUseCase {
    suspend operator fun invoke(): VocabDecks
}

data class VocabDecks(
    val userDecks: List<VocabPracticeDeck>,
    val defaultDecks: List<VocabPracticeDeck>
)

class DefaultGetVocabDecksUseCase(
    private val practiceRepository: PracticeRepository
) : GetVocabDecksUseCase {

    override suspend fun invoke(): VocabDecks {
        return VocabDecks(
            userDecks = emptyList(),
            defaultDecks = vocabDecks
        )
    }

}