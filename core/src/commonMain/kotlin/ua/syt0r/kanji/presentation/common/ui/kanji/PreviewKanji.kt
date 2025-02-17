package ua.syt0r.kanji.presentation.common.ui.kanji

import androidx.compose.ui.graphics.Path
import ua.syt0r.kanji.core.app_data.data.CharacterRadical
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.app_data.data.VocabReading
import ua.syt0r.kanji.core.app_data.data.buildFuriganaString
import ua.syt0r.kanji.core.svg.SvgCommandParser
import ua.syt0r.kanji.core.svg.SvgPathCreator
import kotlin.random.Random

/***
 * Sample kanji for use in previews
 */
object PreviewKanji {

    const val kanji = "間"
    val meanings = listOf("interval", "space")
    val kun = listOf("あい", "あいだ", "ま")
    val on = listOf("カン", "ケン")

    val strokes: List<Path> = listOf(
        "M18.64,15.3c0.71,0.71,1.18,1.82,1.18,3.43c0,3.89-0.05,56.65-0.19,72.77c-0.02,1.92-0.03,4.03-0.05,4.65",
        "M21.01,16.81c5.75-0.6,18.73-2.74,20.5-2.84c1.85-0.1,2.86,0.28,2.9,2.02c0.06,2.75-0.5,16.1-0.85,20.76c-0.12,1.55-0.19,2.57-0.19,2.7",
        "M20.95,27.27c5.99-0.61,14.92-2.02,21.88-2.6",
        "M21.02,39.04c8.11-1.19,14.14-2.1,21.31-2.64",
        "M63.19,13.1c0.98,0.98,1.34,2.15,1.34,2.97c0,5.8-0.08,12.65-0.06,18.93c0.01,2.01,0.02,3.4,0.06,3.58",
        "M65.32,14.77c5.97-0.68,20.69-3.19,22.38-3.28c1.8-0.09,2.81,0.88,2.81,2.82c0,17-0.22,66.12-0.22,78.44c0,10.5-6.35,1.36-7.72,0.23",
        "M65.63,24.79c4.49-0.42,19.73-1.99,23.35-1.99",
        "M65.22,36.07c6.41-0.32,16.53-1.32,23.49-1.81",
        "M40.56,50.95c0.74,0.74,1.04,1.93,1.04,2.99c0,0.83-0.08,20.84-0.05,29.06c0.01,2.25,0.02,2.77,0.05,3",
        "M42.26,52.09c5.56-0.48,19.71-1.98,21.3-2.1c1.68-0.13,2.76,1.46,2.63,2.24c-0.21,1.24-0.41,20.66-0.48,29.02c-0.02,2.29-0.03,3.8-0.03,3.97",
        "M42.64,66.6c5.11-0.48,17.36-1.73,21.88-2.07",
        "M42.25,82.8c5.13-0.3,17-1.55,22.26-2.05"
    )
        .map { SvgCommandParser.parse(it) }
        .map { SvgPathCreator.convert(it) }

    val radicals: List<CharacterRadical> = listOf(
        CharacterRadical("間", "日", 8, 4),
        CharacterRadical("間", "門", 0, 8),
        CharacterRadical("間", "間", 0, 12),
    )

    fun randomKanji() = Random.nextInt(0x4E00, 0x4FFF).toChar().toString()

    val WordWithAlternativesSample = JapaneseWord(
        id = 1,
        reading = VocabReading(
            kanjiReading = "人間",
            kanaReading = "",
            furigana = buildFuriganaString {
                append("人", "にん"); append("間", "げん")
            }
        ),
        listOf("test"),
        emptyList()
    )

    private val SampleWordsPool = listOf<JapaneseWord>(
        JapaneseWord(
            id = 1,
            reading = VocabReading(
                kanjiReading = "期間",
                kanaReading = "",
                furigana = buildFuriganaString {
                    append("期", "き"); append("間", "かん")
                }
            ),
            listOf("test"),
            emptyList()
        ),
        WordWithAlternativesSample
    )

    fun randomWords(number: Int = 10) = (0 until number).map {
        SampleWordsPool[it % SampleWordsPool.size]
    }

    fun randomEncodedWords(number: Int = 10) = randomWords(number).map {
        TODO()
    }

}