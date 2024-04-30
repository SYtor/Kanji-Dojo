package ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.use_case

import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.app_data.data.encodeKanji
import ua.syt0r.kanji.core.japanese.RomajiConverter
import ua.syt0r.kanji.core.japanese.getKanaInfo
import ua.syt0r.kanji.core.japanese.getWordWithExtraRomajiReading
import ua.syt0r.kanji.core.japanese.isKana
import ua.syt0r.kanji.core.user_data.PracticeUserPreferencesRepository
import ua.syt0r.kanji.presentation.common.ui.kanji.parseKanjiStrokes
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingPracticeScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.writing_practice.WritingReviewCharacterDetails

class LoadWritingPracticeCharacterDataUseCase(
    private val appDataRepository: AppDataRepository,
    private val userPreferencesRepository: PracticeUserPreferencesRepository,
    private val romajiConverter: RomajiConverter
) : WritingPracticeScreenContract.LoadCharacterDataUseCase {

    override suspend fun load(character: String): WritingReviewCharacterDetails {
        val strokes = parseKanjiStrokes(appDataRepository.getStrokes(character))
        return when {
            character.first().isKana() -> {
                val kanaInfo = getKanaInfo(character.first())

                val useRomaji = userPreferencesRepository.writingRomajiInsteadOfKanaWords.get()

                val words = appDataRepository.getKanaWords(
                    char = character,
                    limit = WritingPracticeScreenContract.WordsLimit + 1
                )

                val resultWords = if (useRomaji) {
                    words.map { romajiConverter.getWordWithExtraRomajiReading(it) }
                } else {
                    words
                }

                WritingReviewCharacterDetails.KanaReviewDetails(
                    character = character,
                    strokes = strokes,
                    words = resultWords,
                    encodedWords = encodeWords(character, resultWords),
                    kanaSystem = kanaInfo.classification,
                    reading = kanaInfo.reading
                )
            }

            else -> {
                val words = appDataRepository.getWordsWithText(
                    text = character,
                    limit = WritingPracticeScreenContract.WordsLimit + 1
                )
                val encodedWords = encodeWords(character, words)
                val readings = appDataRepository.getReadings(character)
                WritingReviewCharacterDetails.KanjiReviewDetails(
                    character = character,
                    strokes = strokes,
                    radicals = appDataRepository.getRadicalsInCharacter(character),
                    words = words,
                    encodedWords = encodedWords,
                    on = readings.filter { it.value == ReadingType.ON }
                        .keys
                        .toList(),
                    kun = readings.filter { it.value == ReadingType.KUN }
                        .keys
                        .toList(),
                    meanings = appDataRepository.getMeanings(character),
                    variants = appDataRepository.getData(character)
                        ?.variantFamily
                        ?.replace(character, "")
                )
            }
        }
    }

    private fun encodeWords(character: String, words: List<JapaneseWord>): List<JapaneseWord> {
        return words.map { word ->
            word.copy(readings = word.readings.map { it.encodeKanji(character) })
        }
    }

}