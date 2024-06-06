package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration

interface LoadDeckEditVocabDataUseCase {

    suspend operator fun invoke(
        configuration: DeckEditScreenConfiguration.VocabDeck
    ): DeckEditVocabData

}

data class DeckEditVocabData(
    val title: String?,
    val words: List<JapaneseWord>
)

class DefaultLoadDeckEditVocabDataUseCase(
    private val practiceRepository: VocabPracticeRepository,
    private val appDataRepository: AppDataRepository
) : LoadDeckEditVocabDataUseCase {

    override suspend operator fun invoke(
        configuration: DeckEditScreenConfiguration.VocabDeck
    ): DeckEditVocabData = withContext(Dispatchers.IO) {
        when (configuration) {

            is DeckEditScreenConfiguration.VocabDeck.CreateNew -> {
                DeckEditVocabData(null, emptyList())
            }

            is DeckEditScreenConfiguration.VocabDeck.CreateDerived -> {
                DeckEditVocabData(
                    title = configuration.title,
                    words = configuration.words.map { appDataRepository.getWord(it) }
                )
            }

            is DeckEditScreenConfiguration.VocabDeck.Edit -> {
                DeckEditVocabData(
                    title = configuration.title,
                    words = practiceRepository.getDeckWords(configuration.vocabDeckId)
                        .map { appDataRepository.getWord(it) }
                )
            }

        }

    }

}