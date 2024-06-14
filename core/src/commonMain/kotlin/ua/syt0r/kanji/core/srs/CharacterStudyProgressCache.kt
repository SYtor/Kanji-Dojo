package ua.syt0r.kanji.core.srs

import ua.syt0r.kanji.core.logger.Logger
import ua.syt0r.kanji.core.user_data.practice.CharacterStudyProgress
import ua.syt0r.kanji.core.user_data.practice.LetterPracticeRepository

interface CharacterStudyProgressCache {
    fun clear()
    suspend fun get(): Map<String, List<CharacterStudyProgress>>
    suspend fun get(character: String): List<CharacterStudyProgress>
}

class DefaultCharacterStudyProgressCache(
    private val letterPracticeRepository: LetterPracticeRepository
) : CharacterStudyProgressCache {

    private var cache: Map<String, List<CharacterStudyProgress>>? = null

    override fun clear() {
        Logger.logMethod()
        cache = null
    }

    override suspend fun get(): Map<String, List<CharacterStudyProgress>> {
        val currentCache = cache
        return currentCache ?: letterPracticeRepository.getStudyProgresses()
            .groupBy { it.character }
            .also {
                Logger.d("updating character study progress cache")
                cache = it
            }

    }

    override suspend fun get(character: String): List<CharacterStudyProgress> {
        return get()[character] ?: emptyList()
    }

}