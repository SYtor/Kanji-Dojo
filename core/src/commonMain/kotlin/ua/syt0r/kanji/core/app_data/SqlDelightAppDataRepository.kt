package ua.syt0r.kanji.core.app_data

import kotlinx.coroutines.Deferred
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.DetailedJapaneseWord
import ua.syt0r.kanji.core.app_data.data.DetailedVocabReading
import ua.syt0r.kanji.core.app_data.data.DetailedVocabSense
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

    override suspend fun getWord(
        id: Long,
        kanjiReading: String?,
        kanaReading: String
    ): JapaneseWord = vocabQuery {
        getWord(
            id = id,
            kanaReading = kanaReading,
            kanjiReading = kanjiReading
        )
    }

    override suspend fun findWords(
        id: Long?,
        kanjiReading: String?,
        kanaReading: String?
    ): List<JapaneseWord> = vocabQuery {
        val elements = findVocabElementsByIdOrReading(
            entryId = id ?: -1,
            kanjiReading = kanjiReading ?: "",
            kanaReading = kanaReading ?: ""
        ).executeAsList()

        elements.groupBy { it.entry_id }
            .filter { id == null || id == it.key }
            .mapNotNull { (wordId, elements) ->

                when {
                    kanjiReading != null && kanaReading != null -> {
                        getWord(
                            id = wordId,
                            kanaReading = kanaReading,
                            kanjiReading = kanjiReading
                        )
                    }

                    kanjiReading == null && kanaReading == null -> {
                        val element = elements.minByOrNull { it.priority ?: Long.MAX_VALUE }!!
                        getWord(
                            id = wordId,
                            kanaReading = element.reading.takeIf { element.isKana == 1L },
                            kanjiReading = element.reading.takeIf { element.isKana == 0L }
                        )
                    }

                    else -> {
                        val element = elements
                            .sortedBy { it.priority ?: Long.MAX_VALUE }
                            .first { it.reading == kanjiReading || it.reading == kanaReading }
                        getWord(
                            id = wordId,
                            kanaReading = element.reading.takeIf { element.isKana == 1L },
                            kanjiReading = element.reading.takeIf { element.isKana == 0L }
                        )
                    }
                }
            }
    }

    override suspend fun getKanaWords(
        char: String, limit: Int
    ): List<JapaneseWord> = vocabQuery {
        getVocabKanaReadingsLike("%$char%", limit.toLong())
            .executeAsList()
            .map { getWord(it.entry_id, it.reading, null) }
    }

    override suspend fun getSentencesWithTextCount(text: String): Int = vocabQuery {
        getSenseExamplesWithTextCount(text).executeAsOne().toInt()
    }

    override suspend fun getSentencesWithText(
        text: String,
        offset: Int,
        limit: Int
    ): List<Sentence> = vocabQuery {
        getSenseExamplesWithText(text = text, offset = offset.toLong(), limit = limit.toLong())
            .executeAsList()
            .map { Sentence(it.sentence, it.translation) }
    }

    private val delimiter = "|||"
    private val infoIrregularKanji = "iK"
    private val infoIrregularKana = "ik"

    override suspend fun getDetailedWord(id: Long): DetailedJapaneseWord = vocabQuery {
        val senseElements = getVocabSensesWithDetails(id, delimiter).executeAsList()

        val kanjiElements = getVocabKanjiElementsWithDetails(id, delimiter).executeAsList()
        val kanaElements = getVocabKanaElementsWithDetails(id, delimiter).executeAsList()

        val kanjiReadings = kanjiElements.flatMap { kanjiElement ->
            val matchingKanaElements = kanaElements.filter { kanaElement ->
                val restrictedKanji = kanaElement.restricted_kanji?.split(delimiter) ?: emptyList()
                restrictedKanji.isEmpty() || restrictedKanji.contains(kanjiElement.reading)
            }

            val infoList = kanjiElement.informations?.split(delimiter)?.toSet() ?: emptySet()

            matchingKanaElements.map { kanaElement ->
                val kanji = kanjiElement.reading
                val kana = kanaElement.reading
                DetailedVocabReading(
                    kanji = kanji,
                    kana = kana,
                    furigana = searchFurigana(kanji, kana).executeAsOneOrNull()?.parseDBFurigana(),
                    irregular = infoList.contains(infoIrregularKanji),
                    rare = false,
                    outdated = false
                )
            }
        }

        val kanaReadings = kanaElements.map {
            DetailedVocabReading(
                kanji = null,
                kana = it.reading,
                furigana = null,
                irregular = false,
                rare = false,
                outdated = false
            )
        }

        val senseList = senseElements.map {
            val senseKanjiRestrictions = it.kanji_restrictions?.split(delimiter)
                ?.toSet()
                ?: emptySet()

            val senseKanjiReadings = when {
                senseKanjiRestrictions.isEmpty() -> kanjiReadings
                else -> kanaReadings.filter { senseKanjiRestrictions.contains(it.kanji) }
            }

            val senseKanaRestrictions = it.kana_restrictions?.split(delimiter)
                ?.toSet()
                ?: emptySet()

            val senseKanaReadings = when {
                senseKanaRestrictions.isEmpty() -> kanaReadings
                else -> kanaReadings.filter { senseKanaRestrictions.contains(it.kana) }
            }

            DetailedVocabSense(
                glossary = it.glosses?.split(delimiter) ?: emptyList(),
                partOfSpeechList = it.pos?.split(delimiter) ?: emptyList(),
                readings = senseKanjiReadings + senseKanaReadings
            )
        }

        DetailedJapaneseWord(
            id = id,
            senseList = senseList
        )
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
