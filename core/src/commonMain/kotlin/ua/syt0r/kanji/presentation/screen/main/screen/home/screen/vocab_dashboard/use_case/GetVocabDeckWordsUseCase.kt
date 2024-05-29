package ua.syt0r.kanji.presentation.screen.main.screen.home.screen.vocab_dashboard.use_case

import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.JapaneseWord

interface GetVocabDeckWordsUseCase {
    suspend operator fun invoke(ids: List<Long>): List<JapaneseWord>
}

class DefaultGetVocabDeckWordsUseCase(
    private val appDataRepository: AppDataRepository
) : GetVocabDeckWordsUseCase {

    override suspend fun invoke(ids: List<Long>): List<JapaneseWord> {
        return ids.map { appDataRepository.getWord(it) }
    }

}