package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.VocabCardResolver
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.user_data.database.VocabCardData
import ua.syt0r.kanji.core.user_data.database.VocabPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditVocabCard

interface LoadDeckEditVocabDataUseCase {

    suspend operator fun invoke(
        configuration: DeckEditScreenConfiguration.VocabDeck
    ): DeckEditVocabData

}

data class DeckEditVocabData(
    val title: String?,
    val words: List<DeckEditVocabCard>
)

class DefaultLoadDeckEditVocabDataUseCase(
    private val practiceRepository: VocabPracticeRepository,
    private val vocabCardResolver: VocabCardResolver,
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
                val classificationValue = configuration.classification.dbValue
                DeckEditVocabData(
                    title = configuration.title,
                    words = appDataRepository.getWordsWithClassification(classificationValue)
                        .map {
                            val meaning = it.combinedGlossary()
                            val cardData = VocabCardData(
                                kanjiReading = it.reading.kanjiReading,
                                kanaReading = it.reading.kanaReading,
                                meaning = meaning,
                                dictionaryId = it.id
                            )
                            DeckEditVocabCard(
                                data = cardData,
                                savedVocabCard = null,
                                meaning = meaning
                            )
                        }
                )
            }

            is DeckEditScreenConfiguration.VocabDeck.Edit -> {
                val cards = practiceRepository.getAllCards().associateBy { it.cardId }
                DeckEditVocabData(
                    title = configuration.title,
                    words = practiceRepository.getCardIdList(configuration.vocabDeckId)
                        .map {
                            val savedCard = cards.getValue(it)
                            val cardData = savedCard.data
                            val meaning = savedCard.data.meaning
                                ?: vocabCardResolver
                                    .resolveDictionaryCard(
                                        dictionaryId = cardData.dictionaryId,
                                        kanjiReading = cardData.kanjiReading,
                                        kanaReading = cardData.kanaReading
                                    )
                                    .glossary
                                    .joinToString()
                            DeckEditVocabCard(
                                data = savedCard.data,
                                savedVocabCard = savedCard,
                                meaning = meaning
                            )
                        }
                )
            }

        }

    }

}