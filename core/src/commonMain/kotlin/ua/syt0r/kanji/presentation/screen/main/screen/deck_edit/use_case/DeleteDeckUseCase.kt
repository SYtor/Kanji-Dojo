package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case

import ua.syt0r.kanji.core.user_data.database.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.database.VocabPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration

interface DeleteDeckUseCase {
    suspend operator fun invoke(configuration: DeckEditScreenConfiguration)
}

class DefaultDeleteDeckUseCase(
    private val letterPracticeRepository: LetterPracticeRepository,
    private val vocabPracticeRepository: VocabPracticeRepository
) : DeleteDeckUseCase {

    override suspend fun invoke(configuration: DeckEditScreenConfiguration) {
        when (configuration) {
            is DeckEditScreenConfiguration.LetterDeck.Edit -> {
                letterPracticeRepository.deleteDeck(configuration.letterDeckId)
            }

            is DeckEditScreenConfiguration.VocabDeck.Edit -> {
                vocabPracticeRepository.deleteDeck(configuration.vocabDeckId)
            }

            else -> throw IllegalStateException("Trying to delete unsaved deck")
        }
    }

}