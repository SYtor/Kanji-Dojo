package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker.data

import androidx.compose.runtime.Composable
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.presentation.common.resources.string.resolveString

data class LetterDeckPickerItem(
    val previewCharacter: Char,
    val title: @Composable () -> String,
    val classification: CharacterClassification
)


val HiraganaImportItem = LetterDeckPickerItem(
    previewCharacter = 'あ',
    title = { resolveString { letterDeckPicker.hiragana } },
    classification = CharacterClassification.Kana.Hiragana
)

val KatakanaImportItem = LetterDeckPickerItem(
    previewCharacter = 'ア',
    title = { resolveString { letterDeckPicker.katakana } },
    classification = CharacterClassification.Kana.Katakana
)

private val JlptPreviewKanjiList = listOf('一', '言', '合', '軍', '及')
val JlptImportItems: List<LetterDeckPickerItem> = CharacterClassification.JLPT.all
    .zip(JlptPreviewKanjiList)
    .map { (jlpt, previewChar) ->
        LetterDeckPickerItem(
            previewCharacter = previewChar,
            title = { resolveString { letterDeckPicker.jlptItem(jlpt.level) } },
            classification = jlpt
        )
    }

private val GradePreviewKanji = "一万丁不久並丈丑乘".toList()
val GradeImportItems: List<LetterDeckPickerItem> = CharacterClassification.Grade.all
    .zip(GradePreviewKanji)
    .map { (grade, char) ->
        LetterDeckPickerItem(
            previewCharacter = char,
            title = { resolveString { letterDeckPicker.getGradeItem(grade.number) } },
            classification = grade
        )
    }

private val WanikaniPreviewKanji = "上玉矢竹角全辺答受進功悪皆能紀浴是告得裕責援演庁慣接怒攻略更帯酸灰豆熊諾患伴控拉棄析襲刃頃墨幣遂概偶又祥諭庶累匠盲陪亜煩"
    .toList()
val WanikaniImportItems: List<LetterDeckPickerItem> = CharacterClassification.Wanikani.all
    .zip(WanikaniPreviewKanji)
    .map { (classification, char) ->
        LetterDeckPickerItem(
            previewCharacter = char,
            title = { resolveString { letterDeckPicker.wanikaniItem(classification.level) } },
            classification = classification
        )
    }
