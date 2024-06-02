package ua.syt0r.kanji.presentation.screen.main.screen.practice_create.use_case

import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository
import ua.syt0r.kanji.presentation.screen.main.MainDestination
import ua.syt0r.kanji.presentation.screen.main.screen.practice_create.PracticeCreateScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_create.PracticeCreateScreenContract.ScreenState

class PracticeCreateSavePracticeUseCase(
    private val practiceRepository: LetterPracticeRepository
) : PracticeCreateScreenContract.SavePracticeUseCase {

    override suspend fun save(
        configuration: MainDestination.CreatePractice,
        title: String,
        state: ScreenState.Loaded
    ) {
        Logger.logMethod()
        when (configuration) {
            is MainDestination.CreatePractice.EditExisting -> {
                practiceRepository.updatePractice(
                    id = configuration.practiceId,
                    title = title,
                    charactersToAdd = state.characters.toList(),
                    charactersToRemove = state.charactersToRemove.toList()
                )
            }
            else -> {
                practiceRepository.createPractice(
                    characters = state.characters.toList(),
                    title = title
                )
            }
        }
    }

}