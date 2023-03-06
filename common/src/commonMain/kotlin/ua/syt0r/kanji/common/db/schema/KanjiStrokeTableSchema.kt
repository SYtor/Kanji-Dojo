package ua.syt0r.kanji.common.db.schema

object KanjiStrokeTableSchema {

    const val name = "strokes"

    object Columns {
        const val kanji = "kanji"
        const val strokeNumber = "stroke_number"
        const val strokePath = "stroke_path"
    }

}