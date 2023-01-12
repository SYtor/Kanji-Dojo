package ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.use_case

import ua.syt0r.kanji.common.*
import ua.syt0r.kanji.common.db.schema.KanjiReadingTableSchema
import ua.syt0r.kanji.core.kanji_data.KanjiDataRepository
import ua.syt0r.kanji.presentation.common.ui.kanji.parseKanjiStrokes
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.KanjiInfoScreenContract
import ua.syt0r.kanji.presentation.screen.main.screen.kanji_info.KanjiInfoScreenContract.ScreenState
import javax.inject.Inject

class KanjiInfoLoadDataUseCase @Inject constructor(
    private val kanjiDataRepository: KanjiDataRepository
) : KanjiInfoScreenContract.LoadDataUseCase {

    override fun load(character: String): ScreenState.Loaded {
        val char = character.first()
        return when {
            char.isKana() -> getKana(character)
            else -> getKanji(character)
        }
    }

    private fun getKana(character: String): ScreenState.Loaded.Kana {
        val char = character.first()
        val isHiragana = char.isHiragana()
        return ScreenState.Loaded.Kana(
            character = character,
            strokes = getStrokes(character),
            radicals = getRadicals(character),
            words = kanjiDataRepository.getWordsWithCharacter(character),
            kanaSystem = if (isHiragana) {
                CharactersClassification.Kana.HIRAGANA
            } else {
                CharactersClassification.Kana.KATAKANA
            },
            reading = if (isHiragana) {
                hiraganaToRomaji(char)
            } else {
                hiraganaToRomaji(katakanaToHiragana(char))
            },
        )
    }

    private fun getKanji(character: String): ScreenState.Loaded.Kanji {
        val readings = kanjiDataRepository.getReadings(character)
        val kanjiData = kanjiDataRepository.getData(character)
        return ScreenState.Loaded.Kanji(
            character = character,
            strokes = getStrokes(character),
            radicals = getRadicals(character),
            words = kanjiDataRepository.getWordsWithCharacter(character),
            meanings = kanjiDataRepository.getMeanings(character),
            on = readings.filter { it.value == KanjiReadingTableSchema.ReadingType.ON }
                .map { it.key },
            kun = readings.filter { it.value == KanjiReadingTableSchema.ReadingType.KUN }
                .map { it.key },
            grade = kanjiData?.grade,
            jlpt = kanjiData?.jlpt,
            frequency = kanjiData?.frequency
        )
    }

    private fun getStrokes(character: String) =
        parseKanjiStrokes(kanjiDataRepository.getStrokes(character))

    private fun getRadicals(character: String) = kanjiDataRepository
        .getRadicalsInCharacter(character)
        .sortedBy { it.strokesCount }

}