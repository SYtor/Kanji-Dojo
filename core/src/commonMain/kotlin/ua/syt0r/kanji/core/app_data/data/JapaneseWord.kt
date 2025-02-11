package ua.syt0r.kanji.core.app_data.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface VocabReading {

    val textPreview: String
    val furiganaPreview: FuriganaString

    fun appendTo(builder: FuriganaStringBuilder)

    @Serializable
    data class Kana(val reading: String) : VocabReading {

        override val textPreview: String = reading
        override val furiganaPreview: FuriganaString = buildFuriganaString { append(textPreview) }

        override fun appendTo(builder: FuriganaStringBuilder) {
            builder.append(textPreview)
        }

    }

    @Serializable
    data class Kanji(
        val kanjiReading: String,
        val kanaReading: String,
        val furigana: FuriganaString?
    ) : VocabReading {

        override val textPreview: String = kanjiReading
        override val furiganaPreview: FuriganaString =
            furigana ?: buildFuriganaString { append(kanjiReading) }

        override fun appendTo(builder: FuriganaStringBuilder) {
            if (furigana != null) builder.append(furigana) else builder.append(textPreview)
        }

    }

}

@Serializable
data class VocabSense(
    val glossary: List<String>,
    val partOfSpeechList: List<String>
)

@Serializable
data class JapaneseWord(
    val id: Long,
    val displayReading: VocabReading,
    val glossary: List<String>,
    val partOfSpeechList: List<String>
) {

    fun preview() = buildFuriganaString {
        displayReading.appendTo(this)
        append(" ")
        append(glossary.joinToString())
    }

    fun orderedPreview(index: Int) = buildFuriganaString {
        append("${index + 1}. ")
        displayReading.appendTo(this)
        append(" - ")
        append(glossary.joinToString())
    }

    fun orderedPreviewWithHiddenMeaning(index: Int) = buildFuriganaString {
        append("${index + 1}. ")
        append(displayReading.textPreview)
    }

    fun combinedGlossary(): String {
        return glossary.joinToString()
    }

}