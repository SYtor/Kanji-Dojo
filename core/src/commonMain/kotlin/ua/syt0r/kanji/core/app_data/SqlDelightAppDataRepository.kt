package ua.syt0r.kanji.core.app_data

import kotlinx.coroutines.Deferred
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.FuriganaDBEntityCreator
import ua.syt0r.kanji.core.app_data.data.FuriganaString
import ua.syt0r.kanji.core.app_data.data.FuriganaStringCompound
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.KanjiData
import ua.syt0r.kanji.core.app_data.data.RadicalData
import ua.syt0r.kanji.core.app_data.data.ReadingType
import ua.syt0r.kanji.core.app_data.data.VocabReading
import ua.syt0r.kanji.core.app_data.data.VocabSense
import ua.syt0r.kanji.core.app_data.db.AppDataDatabase
import ua.syt0r.kanji.core.appdata.db.LettersQueries
import ua.syt0r.kanji.core.appdata.db.VocabQueries

class SqlDelightAppDataRepository(
    private val deferredDatabase: Deferred<AppDataDatabase>
) : AppDataRepository {

    private suspend fun <T> lettersQuery(
        transactionScope: LettersQueries.() -> T
    ): T {
        val queries = deferredDatabase.await().lettersQueries
        return queries.transactionWithResult { queries.transactionScope() }
    }

    private suspend fun <T> vocabQuery(
        transactionScope: VocabQueries.() -> T
    ): T {
        val queries = deferredDatabase.await().vocabQueries
        return queries.transactionWithResult { queries.transactionScope() }
    }

    override suspend fun getStrokes(character: String): List<String> = lettersQuery {
        getStrokes(character).executeAsList()
    }

    override suspend fun getRadicalsInCharacter(
        character: String
    ): List<CharacterRadical> = lettersQuery {
        getCharacterRadicals(character).executeAsList().map {
            it.run {
                CharacterRadical(
                    character = character,
                    radical = radical,
                    startPosition = start_stroke.toInt(),
                    strokesCount = strokes_count.toInt()
                )
            }
        }
    }

    override suspend fun getMeanings(kanji: String): List<String> = lettersQuery {
        getKanjiMeanings(kanji).executeAsList()
    }

    override suspend fun getReadings(
        kanji: String
    ): Map<String, ReadingType> = lettersQuery {
        getKanjiReadings(kanji).executeAsList().associate { readingData ->
            readingData.reading to ReadingType.entries
                .find { it.value == readingData.reading_type }!!
        }
    }

    override suspend fun getClassificationsForKanji(kanji: String): List<String> = lettersQuery {
        getClassificationsForKanji(kanji).executeAsList()
    }

    override suspend fun getKanjiForClassification(
        classification: String
    ): List<String> = lettersQuery {
        getKanjiWithClassification(classification).executeAsList()
    }

    override suspend fun getCharacterReadingsOfLength(
        length: Int, limit: Int
    ): List<String> = vocabQuery {
        getVocabKanaReadingsOfLength(length.toLong(), limit.toLong()).executeAsList() // TODO review
    }

    override suspend fun getData(kanji: String): KanjiData? = lettersQuery {
        getKanjiData(kanji).executeAsOneOrNull()?.run {
            KanjiData(
                kanji = kanji, frequency = frequency?.toInt(), variantFamily = variantFamily
            )
        }
    }

    override suspend fun getRadicals(): List<RadicalData> = lettersQuery {
        getRadicals().executeAsList().map { RadicalData(it.radical, it.strokesCount.toInt()) }
    }

    override suspend fun getCharactersWithRadicals(
        radicals: List<String>
    ): List<String> = lettersQuery {
        getCharsWithRadicals(radicals, radicals.size.toLong()).executeAsList()
    }

    override suspend fun getAllRadicalsInCharactersWithSelectedRadicals(
        radicals: Set<String>
    ): List<String> = lettersQuery {
        getAllRadicalsInCharactersWithSelectedRadicals(
            radicals,
            radicals.size.toLong()
        ).executeAsList()
    }


    override suspend fun getWordsWithTextCount(text: String): Int = vocabQuery {
        getCountOfVocabWithText(text).executeAsOne().toInt()
    }

    override suspend fun getWordsWithText(
        text: String, offset: Int, limit: Int
    ): List<JapaneseWord> = vocabQuery {
        getReadingsOfVocabWithText(text, offset.toLong(), limit.toLong())
            .executeAsList()
            .map { element ->
                getWord(
                    id = element.entry_id,
                    kanaReading = element.reading.takeIf { element.isKana == 1L },
                    kanjiReading = element.reading.takeIf { element.isKana == 0L }
                )
            }
    }

    override suspend fun findWords(
        id: Long?,
        kanjiReading: String?,
        kanaReading: String?
    ): List<JapaneseWord> = vocabQuery {
        findVocabElementsByIdOrReading(
            entryId = id ?: -1,
            kanjiReading = kanaReading ?: "",
            kanaReading = kanaReading ?: ""
        ).executeAsList()
            .map { element ->
                getWord(
                    id = element.entry_id,
                    kanaReading = element.reading.takeIf { element.isKana == 1L },
                    kanjiReading = element.reading.takeIf { element.isKana == 0L }
                )
            }
    }

    override suspend fun getKanaWords(
        char: String, limit: Int
    ): List<JapaneseWord> = vocabQuery {
        getVocabKanaReadingsLike("%$char%", limit.toLong())
            .executeAsList()
            .map { getWord(it.entry_id, it.reading, null) }
    }

    override suspend fun getWordsWithClassificationCount(classification: String): Int = vocabQuery {
        getVocabImportsForClassificationCount(classification).executeAsOne().toInt()
    }

    override suspend fun getWordsWithClassification(
        classification: String
    ): List<JapaneseWord> = vocabQuery {
        getVocabImportsForClassification(classification)
            .executeAsList()
            .map { vocabImport ->
                JapaneseWord(
                    id = vocabImport.jmdict_seq,
                    reading = VocabReading(
                        kanjiReading = vocabImport.kanji,
                        kanaReading = vocabImport.kana,
                        furigana = vocabImport.kanji?.let {
                            searchFurigana(vocabImport.kanji, vocabImport.kana)
                                .executeAsOneOrNull()
                                ?.parseDBFurigana()
                        }
                    ),
                    glossary = listOf(vocabImport.definition),
                    partOfSpeechList = emptyList()
                )
            }
    }

    private fun VocabQueries.getWord(
        id: Long,
        kanaReading: String?,
        kanjiReading: String?,
    ): JapaneseWord {

        val reading: VocabReading
        val sense: VocabSense

        when {
            kanjiReading != null -> {
                val kanaReading = kanaReading ?: getVocabKanaElements(id).executeAsList().let {
                    getVocabRestrictedKanaElements(id, kanjiReading).executeAsList()
                        .firstOrNull()?.reading
                        ?: getVocabKanaElements(id).executeAsList().first().reading
                }

                val furigana = searchFurigana(kanjiReading, kanaReading)
                    .executeAsOneOrNull()
                    ?.parseDBFurigana()
                reading = VocabReading(kanjiReading, kanaReading, furigana)

                val senseRestrictions = getKanjiReadingRestrictedSenses(id, kanjiReading)
                    .executeAsList()
                sense = getWordSenses(id, senseRestrictions).first()
            }

            else -> {
                reading = VocabReading(null, kanaReading!!, null)
                val senseRestrictions = getKanaReadingRestrictedSenses(id, kanaReading)
                    .executeAsList()
                sense = getWordSenses(id, senseRestrictions).first()
            }
        }

        return JapaneseWord(
            id = id,
            reading = reading,
            glossary = sense.glossary,
            partOfSpeechList = sense.partOfSpeechList
        )
    }

    private fun VocabQueries.getWordSenses(
        wordId: Long,
        restrictedSenseIdList: List<Long>
    ): Sequence<VocabSense> {
        val isNoRestrictions = restrictedSenseIdList.isEmpty()
        return getVocabSenses(wordId).executeAsList().asSequence()
            .filter { isNoRestrictions || restrictedSenseIdList.contains(it) }
            .map { senseId ->
                VocabSense(
                    glossary = getVocabSenseGlosses(senseId).executeAsList(),
                    partOfSpeechList = getPartOfSpeechWithDescriptionsForVocabSense(senseId)
                        .executeAsList()
                        .map { it.explanation }
                )
            }
    }

    private fun String.parseDBFurigana(): FuriganaString = FuriganaDBEntityCreator
        .fromJsonString(this)
        .map { FuriganaStringCompound(it.text, it.annotation) }
        .let { FuriganaString(it) }

}
