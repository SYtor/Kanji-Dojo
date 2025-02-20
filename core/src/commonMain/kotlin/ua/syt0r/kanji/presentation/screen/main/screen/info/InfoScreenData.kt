package ua.syt0r.kanji.presentation.screen.main.screen.info

import androidx.compose.ui.graphics.Path
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.app_data.data.JapaneseWord
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.core.japanese.KanaReading
import ua.syt0r.kanji.presentation.common.Paginateable
import ua.syt0r.kanji.presentation.common.ui.kanji.KanjiRadicalsSectionData

@Serializable
sealed interface InfoScreenData {

    @Serializable
    data class Letter(
        val letter: String
    ) : InfoScreenData

    @Serializable
    data class Vocab(
        val id: Long?,
        val kanjiReading: String?,
        val kanaReading: String?
    ) : InfoScreenData

}

fun JapaneseWord.toInfoScreenData() = InfoScreenData.Vocab(
    id = id,
    kanjiReading = reading.kanjiReading,
    kanaReading = reading.kanaReading
)

sealed interface LetterInfoData {

    val character: String
    val strokes: List<Path>

    val vocab: Paginateable<JapaneseWord>

    data class Kana(
        override val character: String,
        override val strokes: List<Path>,
        val kanaSystem: CharacterClassification.Kana,
        val reading: KanaReading,
        override val vocab: Paginateable<JapaneseWord>,
    ) : LetterInfoData

    data class Kanji(
        override val character: String,
        override val strokes: List<Path>,
        val on: List<String>,
        val kun: List<String>,
        val meanings: List<String>,
        val grade: Int?,
        val jlptLevel: Int?,
        val frequency: Int?,
        val radicalsSectionData: KanjiRadicalsSectionData,
        val displayRadicals: List<String>,
        override val vocab: Paginateable<JapaneseWord>,
    ) : LetterInfoData

}