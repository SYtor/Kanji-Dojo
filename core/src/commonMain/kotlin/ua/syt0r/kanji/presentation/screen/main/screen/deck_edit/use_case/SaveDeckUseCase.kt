package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case

import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.database.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.database.VocabPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditItemAction
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditListItem
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.LetterDeckEditListItem
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.VocabDeckEditListItem

interface SaveDeckUseCase {
    suspend operator fun invoke(
        configuration: DeckEditScreenConfiguration,
        title: String,
        list: List<DeckEditListItem>
    )
}

class DefaultSaveDeckUseCase(
    private val letterPracticeRepository: LetterPracticeRepository,
    private val vocabPracticeRepository: VocabPracticeRepository
) : SaveDeckUseCase {

    override suspend fun invoke(
        configuration: DeckEditScreenConfiguration,
        title: String,
        list: List<DeckEditListItem>
    ) {
        Logger.logMethod()
        when (configuration) {
            is DeckEditScreenConfiguration.LetterDeck.CreateNew,
            is DeckEditScreenConfiguration.LetterDeck.CreateDerived -> {
                letterPracticeRepository.createDeck(
                    title = title,
                    characters = list.filter<LetterDeckEditListItem>(DeckEditItemAction.Add)
                        .map { it.character },
                )
            }

            is DeckEditScreenConfiguration.LetterDeck.Edit -> {
                letterPracticeRepository.updateDeck(
                    id = configuration.letterDeckId,
                    title = title,
                    charactersToAdd = list.filter<LetterDeckEditListItem>(DeckEditItemAction.Add)
                        .map { it.character },
                    charactersToRemove = list.filter<LetterDeckEditListItem>(DeckEditItemAction.Remove)
                        .map { it.character }
                )
            }

            DeckEditScreenConfiguration.VocabDeck.CreateNew,
            is DeckEditScreenConfiguration.VocabDeck.CreateDerived -> {
                vocabPracticeRepository.createDeck(
                    title = title,
                    words = list.filter<VocabDeckEditListItem>(DeckEditItemAction.Add)
                        .map { it.card.data }
                )
            }

            is DeckEditScreenConfiguration.VocabDeck.Edit -> {
                vocabPracticeRepository.updateDeck(
                    id = configuration.vocabDeckId,
                    title = title,
                    cardsToAdd = list.filter<VocabDeckEditListItem>(DeckEditItemAction.Add)
                        .map { it.card.modifiedData.value ?: it.card.data },
                    cardsToUpdate = list.filter<VocabDeckEditListItem>(DeckEditItemAction.Nothing)
                        .mapNotNull {
                            it.card.savedVocabCard?.copy(
                                data = it.card.modifiedData.value ?: return@mapNotNull null
                            )
                        },
                    cardsToRemove = list.filter<VocabDeckEditListItem>(DeckEditItemAction.Remove)
                        .mapNotNull { it.card.savedVocabCard?.cardId }
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : DeckEditListItem> List<DeckEditListItem>.filter(
        action: DeckEditItemAction
    ): List<T> {
        return filter { it.action.value == action } as List<T>
    }

}