package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data

import androidx.compose.material3.MaterialTheme
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme


private val HiraganaImportItem = DeckPickerDeck(
    previewText = "あ",
    title = { deckPicker.hiragana },
    classification = CharacterClassification.Kana.Hiragana
)

private val KatakanaImportItem = DeckPickerDeck(
    previewText = "ア",
    title = { deckPicker.katakana },
    classification = CharacterClassification.Kana.Katakana
)

private val KanaDeckPickerCategory = DeckPickerCategory(
    title = { deckPicker.kanaTitle },
    description = {
        val color = MaterialTheme.extraColorScheme.link
        deckPicker.kanaDescription(color)
    },
    items = listOf(HiraganaImportItem, KatakanaImportItem)
)

private val JlptPreviewKanjiList = listOf("一", "言", "合", "軍", "及")
private val JlptImportItems: List<DeckPickerDeck> = CharacterClassification.JLPT.all
    .zip(JlptPreviewKanjiList)
    .map { (jlpt, previewChar) ->
        DeckPickerDeck(
            previewText = previewChar,
            title = { deckPicker.jlptItem(jlpt.level) },
            classification = jlpt
        )
    }

private val JlptDeckPickerCategory = DeckPickerCategory(
    title = { deckPicker.jltpTitle },
    description = {
        val color = MaterialTheme.extraColorScheme.link
        deckPicker.jlptDescription(color)
    },
    items = JlptImportItems
)

private val GradePreviewKanji = "一万丁不久並丈丑乘".toList()
private val GradeImportItems: List<DeckPickerDeck> = CharacterClassification.Grade.all
    .zip(GradePreviewKanji)
    .map { (grade, char) ->
        DeckPickerDeck(
            previewText = char.toString(),
            title = { deckPicker.getGradeItem(grade.number) },
            classification = grade
        )
    }

private val GradeDeckPickerCategory = DeckPickerCategory(
    title = { deckPicker.gradeTitle },
    description = {
        val color = MaterialTheme.extraColorScheme.link
        deckPicker.gradeDescription(color)
    },
    items = GradeImportItems
)

private val WanikaniPreviewKanji =
    "上玉矢竹角全辺答受進功悪皆能紀浴是告得裕責援演庁慣接怒攻略更帯酸灰豆熊諾患伴控拉棄析襲刃頃墨幣遂概偶又祥諭庶累匠盲陪亜煩"
        .toList()
private val WanikaniImportItems: List<DeckPickerDeck> = CharacterClassification.Wanikani.all
    .zip(WanikaniPreviewKanji)
    .map { (classification, char) ->
        DeckPickerDeck(
            previewText = char.toString(),
            title = { deckPicker.wanikaniItem(classification.level) },
            classification = classification
        )
    }

private val WanikaniImportCategory = DeckPickerCategory(
    title = { deckPicker.wanikaniTitle },
    description = {
        val color = MaterialTheme.extraColorScheme.link
        deckPicker.wanikaniDescription(color)
    },
    items = WanikaniImportItems
)

val DeckPickerLetterCategories = listOf(
    KanaDeckPickerCategory,
    JlptDeckPickerCategory,
    GradeDeckPickerCategory,
    WanikaniImportCategory
)
