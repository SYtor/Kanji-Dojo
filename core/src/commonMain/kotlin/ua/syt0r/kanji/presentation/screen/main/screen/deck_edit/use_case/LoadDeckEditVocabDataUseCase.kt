package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.user_data.database.VocabCardData
import ua.syt0r.kanji.core.user_data.database.VocabPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditItemAction
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.VocabDeckEditListItem

interface LoadDeckEditVocabDataUseCase {

    suspend operator fun invoke(
        configuration: DeckEditScreenConfiguration.VocabDeck,
        defaultListItemAction: DeckEditItemAction
    ): DeckEditVocabData

}

data class DeckEditVocabData(
    val title: String?,
    val items: List<VocabDeckEditListItem>
)

class DefaultLoadDeckEditVocabDataUseCase(
    private val practiceRepository: VocabPracticeRepository,
    private val appDataRepository: AppDataRepository
) : LoadDeckEditVocabDataUseCase {

    override suspend operator fun invoke(
        configuration: DeckEditScreenConfiguration.VocabDeck,
        defaultListItemAction: DeckEditItemAction
    ): DeckEditVocabData = withContext(Dispatchers.IO) {

        when (configuration) {

            is DeckEditScreenConfiguration.VocabDeck.CreateNew -> {
                DeckEditVocabData(null, emptyList())
            }

            is DeckEditScreenConfiguration.VocabDeck.CreateDerived -> {
                val classificationValue = configuration.classification.dbValue
                val words = appDataRepository.getWordsWithClassification(classificationValue)

                val wordIdSet = words.map { it.id }.toSet()
                val senseList = appDataRepository.getWordSenses(wordIdSet).associateBy { it.wordId }

                DeckEditVocabData(
                    title = configuration.title,
                    items = words.map {
                        val meaning = it.combinedGlossary()
                        val cardData = VocabCardData(
                            kanjiReading = it.reading.kanjiReading,
                            kanaReading = it.reading.kanaReading,
                            meaning = meaning,
                            dictionaryId = it.id
                        )
                        VocabDeckEditListItem(
                            cardData = cardData,
                            savedVocabCard = null,
                            dictionarySenseList = senseList.getValue(it.id).senseList,
                            initialAction = defaultListItemAction
                        )
                    }
                )
            }

            is DeckEditScreenConfiguration.VocabDeck.Edit -> {
                val cardsCache = practiceRepository.getAllCards().associateBy { it.cardId }
                val deckCards = practiceRepository.getCardIdList(configuration.vocabDeckId)
                    .map { cardsCache.getValue(it) }
                val wordIdSet = deckCards.map { it.data.dictionaryId }.toSet()
                val senseList = appDataRepository.getWordSenses(wordIdSet)
                    .associateBy { it.wordId }

                val deckEditCards = deckCards.map {
                    val savedCard = cardsCache.getValue(it.cardId)
                    val sense = senseList.getValue(it.data.dictionaryId).senseList
                    VocabDeckEditListItem(
                        cardData = savedCard.data,
                        savedVocabCard = savedCard,
                        dictionarySenseList = sense,
                        initialAction = defaultListItemAction
                    )
                }

                DeckEditVocabData(
                    title = configuration.title,
                    items = deckEditCards
                )
            }
        }

    }

}