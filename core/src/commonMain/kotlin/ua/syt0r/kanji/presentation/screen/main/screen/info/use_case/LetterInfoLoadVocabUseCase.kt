package ua.syt0r.kanji.presentation.screen.main.screen.info.use_case

import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.presentation.screen.main.screen.info.InfoScreenContract

class LetterInfoLoadVocabUseCase(
    private val appDataRepository: AppDataRepository
) : InfoScreenContract.LoadCharacterWordsUseCase {

    override suspend fun load(character: String, offset: Int, limit: Int): List<JapaneseWord> {
        return appDataRepository.getWordsWithText(character, offset, limit)
    }

}