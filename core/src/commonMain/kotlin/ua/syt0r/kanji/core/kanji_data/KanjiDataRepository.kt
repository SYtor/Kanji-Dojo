package ua.syt0r.kanji.core.kanji_data

import ua.syt0r.kanji.common.CharactersClassification
import ua.syt0r.kanji.common.db.entity.CharacterRadical
import ua.syt0r.kanji.common.db.schema.KanjiReadingTableSchema
import ua.syt0r.kanji.core.kanji_data.data.JapaneseWord
import ua.syt0r.kanji.core.kanji_data.data.KanjiData
import ua.syt0r.kanji.core.kanji_data.data.RadicalData

interface KanjiDataRepository {

    fun getStrokes(kanji: String): List<String>
    fun getRadicalsInCharacter(character: String): List<CharacterRadical>

    fun getMeanings(kanji: String): List<String>
    fun getReadings(kanji: String): Map<String, KanjiReadingTableSchema.ReadingType>
    fun getData(kanji: String): KanjiData?
    fun getCharacterClassifications(kanji: String): List<CharactersClassification>

    fun getKanjiByClassification(classification: CharactersClassification): List<String>
    fun getWordsWithText(text: String, limit: Int = Int.MAX_VALUE): List<JapaneseWord>
    fun getKanaWords(char: String, limit: Int = Int.MAX_VALUE): List<JapaneseWord>

    fun getRadicals(): List<RadicalData>
    fun getCharactersWithRadicals(radicals: List<String>): List<String>
    fun getAllRadicalsInCharacters(characters: List<String>): List<String>

}