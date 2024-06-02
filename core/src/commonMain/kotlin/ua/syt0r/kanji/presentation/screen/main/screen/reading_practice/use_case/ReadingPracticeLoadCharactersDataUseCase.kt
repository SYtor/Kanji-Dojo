package ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.use_case

import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.japanese.RomajiConverter
import ua.syt0r.kanji.core.japanese.getKanaInfo
import ua.syt0r.kanji.core.japanese.getWordWithFuriganaForKana
import ua.syt0r.kanji.core.japanese.isKana
import ua.syt0r.kanji.core.user_data.preferences.PracticeUserPreferencesRepository
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ReadingPracticeContract
import ua.syt0r.kanji.presentation.screen.main.screen.reading_practice.ReadingReviewCharacterData

class ReadingPracticeLoadCharactersDataUseCase(
    private val appDataRepository: AppDataRepository,
    private val userPreferencesRepository: PracticeUserPreferencesRepository,
    private val romajiConverter: RomajiConverter
) : ReadingPracticeContract.LoadCharactersDataUseCase {

    override suspend fun load(character: String): ReadingReviewCharacterData {
        val char = character.first()
        return when {
            char.isKana() -> {
                val kanaInfo = getKanaInfo(char)

                val words = appDataRepository.getKanaWords(
                    char = character,
                    limit = ReadingPracticeContract.DisplayWordsLimit
                )

                val kanaRomaji = userPreferencesRepository.readingRomajiFuriganaForKanaWords.get()

                val resultWords = if (kanaRomaji) {
                    words.map { romajiConverter.getWordWithFuriganaForKana(it) }
                } else {
                    words
                }

                ReadingReviewCharacterData.Kana(
                    reading = kanaInfo.reading,
                    classification = kanaInfo.classification,
                    character = character,
                    words = resultWords
                )
            }

            else -> {
                val readings = appDataRepository.getReadings(character)
                ReadingReviewCharacterData.Kanji(
                    character = character,
                    on = readings
                        .filter { it.value == ReadingType.ON }
                        .map { it.key },
                    kun = readings
                        .filter { it.value == ReadingType.KUN }
                        .map { it.key },
                    meanings = appDataRepository.getMeanings(character),
                    words = appDataRepository.getWordsWithText(
                        text = character,
                        limit = ReadingPracticeContract.DisplayWordsLimit
                    )
                )
            }
        }
    }

}
