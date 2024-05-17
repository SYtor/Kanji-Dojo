package ua.syt0r.kanji.core.app_data

import kotlinx.coroutines.Deferred
import ua.syt0r.kanji.BuildKonfig
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.KanjiData
import ua.syt0r.kanji.core.app_data.data.RadicalData
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.app_data.db.AppDataDatabase

val AppDataDatabaseVersion: Long = BuildKonfig.appDataDatabaseVersion
val AppDataDatabaseResourceName: String = BuildKonfig.appDataAssetName

interface AppDataDatabaseProvider {
    fun provideAsync(): Deferred<AppDataDatabase>
}

interface AppDataRepository {

    suspend fun getStrokes(character: String): List<String>
    suspend fun getRadicalsInCharacter(character: String): List<CharacterRadical>

    suspend fun getMeanings(kanji: String): List<String>
    suspend fun getReadings(kanji: String): Map<String, ReadingType>
    suspend fun getCharacterReadingsOfLength(length: Int, limit: Int): List<String>
    suspend fun getData(kanji: String): KanjiData?

    suspend fun getWordsWithTextCount(text: String): Int
    suspend fun getWordsWithText(
        text: String,
        offset: Int = 0,
        limit: Int = Int.MAX_VALUE
    ): List<JapaneseWord>

    suspend fun getKanaWords(char: String, limit: Int = Int.MAX_VALUE): List<JapaneseWord>
    suspend fun getWord(id: Long): JapaneseWord

    suspend fun getRadicals(): List<RadicalData>
    suspend fun getCharactersWithRadicals(radicals: List<String>): List<String>
    suspend fun getAllRadicalsInCharactersWithSelectedRadicals(radicals: Set<String>): List<String>

}