package ua.syt0r.kanji.core.app_data.data

import kotlinx.serialization.Serializable

@Serializable
data class JapaneseWord(
    val id: Long,
    val readings: List<FuriganaString>,
    val meanings: List<String>
) {

    fun preview() = buildFuriganaString {
        append(readings.first())
        append(" - ")
        append(meanings.first())
    }

    fun orderedPreview(index: Int) = buildFuriganaString {
        append("${index + 1}. ")
        append(readings.first())
        append(" - ")
        append(meanings.first())
    }

    fun orderedPreviewWithHiddenMeaning(index: Int) = buildFuriganaString {
        append("${index + 1}. ")
        append(readings.first().withEmptyFurigana())
    }

}