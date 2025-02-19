package ua.syt0r.kanji.core.app_data.data

import kotlinx.serialization.Serializable


@Serializable
data class VocabReading(
    val kanjiReading: String?,
    val kanaReading: String,
    val furigana: FuriganaString?
)

fun formattedVocabReading(
    kanaReading: String,
    kanjiReading: String? = null,
    furigana: FuriganaString? = null,
    linebreak: Boolean = false
): FuriganaString = when {
    furigana != null -> {
        furigana
    }

    kanjiReading != null -> buildFuriganaString {
        append(kanjiReading)
        if (linebreak) append("\n")
        append("【${kanaReading}】")
    }

    else -> buildFuriganaString {
        append(kanaReading)
    }
}

fun formattedVocabStringReading(
    kanaReading: String,
    kanjiReading: String? = null,
): String = when {
    kanjiReading != null -> buildString {
        append(kanjiReading)
        append("【${kanaReading}】")
    }

    else -> kanaReading
}

@Serializable
data class VocabSense(
    val glossary: List<String>,
    val partOfSpeechList: List<String>
)

@Serializable
data class JapaneseWord(
    val id: Long,
    val reading: VocabReading,
    val glossary: List<String>,
    val partOfSpeechList: List<String>
) {

    fun combinedGlossary(): String {
        return glossary.joinToString()
    }

}

data class DetailedVocabSense(
    val glossary: List<String>,
    val partOfSpeechList: List<String>,
    val readings: List<DetailedVocabReading>
)

data class DetailedVocabReading(
    val kanji: String?,
    val kana: String,
    val furigana: FuriganaString?,
    val irregular: Boolean,
    val rare: Boolean,
    val outdated: Boolean
)

data class DetailedJapaneseWord(
    val id: Long,
    val senseList: List<DetailedVocabSense>
)