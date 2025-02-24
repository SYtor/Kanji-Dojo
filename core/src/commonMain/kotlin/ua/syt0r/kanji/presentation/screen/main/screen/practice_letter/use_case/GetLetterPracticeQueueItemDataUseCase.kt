package ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.use_case

import ua.syt0r.kanji.core.app_data.AppDataRepository
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.japanese.RomajiConverter
import ua.syt0r.kanji.core.japanese.getKanaInfo
import ua.syt0r.kanji.core.japanese.isKana
import ua.syt0r.kanji.presentation.common.ui.kanji.parseKanjiStrokes
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.LetterPracticeScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeExampleWord
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeItemData
import ua.syt0r.kanji.presentation.screen.main.screen.practice_letter.data.LetterPracticeQueueItemDescriptor

interface GetLetterPracticeQueueItemDataUseCase {
    suspend operator fun invoke(
        descriptor: LetterPracticeQueueItemDescriptor
    ): LetterPracticeItemData
}

class DefaultGetLetterPracticeQueueItemDataUseCase(
    private val appDataRepository: AppDataRepository,
    private val romajiConverter: RomajiConverter
) : GetLetterPracticeQueueItemDataUseCase {

    override suspend fun invoke(
        descriptor: LetterPracticeQueueItemDescriptor
    ): LetterPracticeItemData {

        val isKana = descriptor.character.first().isKana()

        val words: List<LetterPracticeExampleWord> = when {
            isKana -> {
                appDataRepository.getKanaWords(
                    char = descriptor.character,
                    limit = LetterPracticeScreenContract.WordsLimit + 1
                ).map {
                    LetterPracticeExampleWord(
                        word = it,
                        romaji = when {
                            descriptor.romajiReading -> romajiConverter.toRomaji(it.reading.kanaReading)
                            else -> null
                        }
                    )
                }
            }

            else -> appDataRepository.getWordsWithText(
                text = descriptor.character,
                limit = LetterPracticeScreenContract.WordsLimit + 1
            ).map { LetterPracticeExampleWord(it, null) }
        }

        return when (descriptor) {
            is LetterPracticeQueueItemDescriptor.Writing -> {
                getWritingItemData(
                    character = descriptor.character,
                    isKana = isKana,
                    words = words
                )
            }

            is LetterPracticeQueueItemDescriptor.Reading -> {
                getReadingItemData(
                    character = descriptor.character,
                    isKana = isKana,
                    words = words
                )
            }
        }
    }

    private suspend fun getWritingItemData(
        character: String,
        isKana: Boolean,
        words: List<LetterPracticeExampleWord>
    ): LetterPracticeItemData {
        val strokes = parseKanjiStrokes(appDataRepository.getStrokes(character))
        return when {
            isKana -> {
                val kanaInfo = getKanaInfo(character.first())

                LetterPracticeItemData.KanaWritingData(
                    character = character,
                    strokes = strokes,
                    words = words,
                    kanaSystem = kanaInfo.classification,
                    reading = kanaInfo.reading
                )
            }

            else -> {
                val readings = appDataRepository.getReadings(character)
                LetterPracticeItemData.KanjiWritingData(
                    character = character,
                    strokes = strokes,
                    radicals = appDataRepository.getRadicalsInCharacter(character),
                    words = words,
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

    private suspend fun getReadingItemData(
        character: String,
        isKana: Boolean,
        words: List<LetterPracticeExampleWord>
    ): LetterPracticeItemData.ReadingData {
        return when {
            isKana -> {
                val kanaInfo = getKanaInfo(character.first())
                LetterPracticeItemData.KanaReadingData(
                    character = character,
                    words = words,
                    kanaSystem = kanaInfo.classification,
                    reading = kanaInfo.reading
                )
            }

            else -> {
                val readings = appDataRepository.getReadings(character)
                LetterPracticeItemData.KanjiReadingData(
                    character = character,
                    words = words,
                    radicals = appDataRepository.getRadicalsInCharacter(character),
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

}