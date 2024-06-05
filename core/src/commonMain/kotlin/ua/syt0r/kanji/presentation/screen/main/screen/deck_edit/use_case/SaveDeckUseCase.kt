package ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.use_case

import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.core.user_data.practice.VocabPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditScreenContract.ScreenState
import ua.syt0r.kanji.presentation.screen.main.screen.deck_edit.DeckEditItemAction

interface SaveDeckUseCase {
    suspend operator fun invoke(
        configuration: DeckEditScreenConfiguration,
        state: ScreenState.Loaded
    )
}

class DefaultSaveDeckUseCase(
    private val letterPracticeRepository: LetterPracticeRepository,
    private val vocabPracticeRepository: VocabPracticeRepository
) : SaveDeckUseCase {

    override suspend fun invoke(
        configuration: DeckEditScreenConfiguration,
        state: ScreenState.Loaded
    ) {
        Logger.logMethod()
        when (configuration) {
            is DeckEditScreenConfiguration.LetterDeck.CreateNew,
            is DeckEditScreenConfiguration.LetterDeck.CreateDerived -> {
                state as ScreenState.LetterDeckEditing
                letterPracticeRepository.createPractice(
                    characters = state.listState.value.mapNotNull {
                        if (it.action.value == DeckEditItemAction.Add) it.character else null
                    },
                    title = state.title.value
                )
            }

            is DeckEditScreenConfiguration.LetterDeck.Edit -> {
                state as ScreenState.LetterDeckEditing
                letterPracticeRepository.updatePractice(
                    id = configuration.letterDeckId,
                    title = state.title.value,
                    charactersToAdd = state.listState.value.mapNotNull {
                        if (it.action.value == DeckEditItemAction.Add) it.character else null
                    },
                    charactersToRemove = state.listState.value.mapNotNull {
                        if (it.action.value == DeckEditItemAction.Remove) it.character else null
                    }
                )
            }

            DeckEditScreenConfiguration.VocabDeck.CreateNew,
            is DeckEditScreenConfiguration.VocabDeck.CreateDerived -> {

            }

            is DeckEditScreenConfiguration.VocabDeck.Edit -> {

            }
        }
    }

}