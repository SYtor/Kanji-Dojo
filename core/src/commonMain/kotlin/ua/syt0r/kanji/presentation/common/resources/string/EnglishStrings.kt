package ua.syt0r.kanji.presentation.common.resources.string

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import ua.syt0r.kanji.presentation.common.withClickableUrl
import kotlin.time.Duration

object EnglishStrings : Strings {

    override val appName: String = "Kanji Dojo"

    override val homeTitle: String = "Kanji Dojo"
    override val homeTabDashboard: String = "Practice"
    override val homeTabSearch: String = "Search"
    override val homeTabSettings: String = "Settings"

    override val practiceDashboard = EnglishPracticeDashboardStrings
    override val createPracticeDialog = EnglishCreatePracticeDialogStrings
    override val search: SearchStrings = EnglishSearchStrings
    override val alternativeDialog: AlternativeDialogStrings = EnglishAlternativeDialogStrings
    override val settings: SettingsStrings = EnglishSettingsStrings
    override val about: AboutStrings = EnglishAboutStrings
    override val practiceImport: PracticeImportStrings = EnglishPracticeImportStrings

    override val urlPickerMessage: String = "Open With"
    override val urlPickerErrorMessage: String = "Web browser not found"

}

object EnglishPracticeDashboardStrings : PracticeDashboardStrings {
    override val emptyMessage = { color: Color ->
        buildAnnotatedString {
            append("Click ")
            withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) { append("+") }
            append(" and save practice to start.\nPractices are used to track your progress")
        }
    }
    override val itemTimeMessage: (Duration?) -> String = {
        "Reviewed:" + when {
            it == null -> "Never"
            it.inWholeDays == 1L -> "${it.inWholeDays} day ago"
            it.inWholeDays > 0 -> "${it.inWholeDays} days ago"
            else -> "Today"
        }
    }
    override val analyticsSuggestionMessage: String =
        "Please consider enabling analytics reports. This data will help improve application. It can always be disabled later in settings screen"
    override val analyticsSuggestionAction: String = "Enable"
}

object EnglishCreatePracticeDialogStrings : CreatePracticeDialogStrings {
    override val title: String = "Create practice"
    override val selectMessage: String = "Select (Kana, JLPT, etc.)"
    override val createMessage: String = "Create custom"
}

object EnglishSearchStrings : SearchStrings {
    override val inputHint: String = "Search for character or words"
    override val charactersTitle: (count: Int) -> String = { "Characters ($it)" }
    override val wordsTitle: (count: Int) -> String = { "Expressions ($it)" }
    override val radicalsSheetTitle: String = "Search by radicals"
    override val radicalsFoundCharacters: String = "Found characters"
    override val radicalsEmptyFoundCharacters: String = "Nothing found"
    override val radicalSheetRadicalsSectionTitle: String = "Radicals"
}

object EnglishAlternativeDialogStrings : AlternativeDialogStrings {
    override val title: String = "Alternative expressions"
    override val readingsTitle: String = "Readings"
    override val meaningsTitle: String = "Meanings"
    override val button: String = "Close"
}

object EnglishSettingsStrings : SettingsStrings {
    override val noTranslationLayoutTitle: String = "No translation layout"
    override val noTranslationLayoutMessage: String =
        "Hides character translations during writing practice"
    override val analyticsTitle: String = "Analytics"
    override val analyticsMessage: String = "Allow sending anonymous data to improve experience"
    override val aboutTitle: String = "About"
}

object EnglishAboutStrings : AboutStrings {

    override val title: String = "About"
    override val version: String = "Version: %s"
    override val description: String =
        "Hone your Japanese writing skills with Kanji Dojo. Pick or create your own list of characters to train. Suitable for both complete beginners and advanced learners. All content is absolutely free and more features are coming"
    override val githubTitle: String = "Github"
    override val githubDescription: String = "Source code &amp; development"
    override val creditsTitle: String = "Credits"
    override val licenseTemplate: (String) -> String = { "License: $it" }
    override val licenseKanjiVgTitle: String = "KaniVG"
    override val licenseKanjiVgDescription: String =
        "Provides writing strokes, radicals information"
    override val licenseKanjiDicTitle: String = "Kanji Dic"
    override val licenseKanjiDicDescription: String =
        "Provides characters info, such as meanings, readings and classifications"
    override val licenseTanosTitle: String = "Tanos by Jonathan Waller"
    override val licenseTanosDescription: String = "Provides JLPT classification for kanji"
    override val licenseJmDictTitle: String = "JMDict"
    override val licenseJmDictDescription: String =
        "Japanese-Multilingual dictionary, provides expressions"
    override val licenseJmDictFuriganaTitle: String = "JmdictFurigana"
    override val licenseJmDictFuriganaDescription: String =
        "Open-source furigana resource to complement the EDICT/Jmdict and ENAMDICT/Jmnedict dictionary files"
    override val licenseLeedsCorpusTitle: String = "Frequency list by Leeds university"
    override val licenseLeedsCorpusDescription: String =
        "Words ranking by frequency of usage in internet"
    override val licenseCCASA3: String = "Creative Commons Attribution-Share Alike 3.0"
    override val licenseCCASA4: String = "Creative Commons Attribution-Share Alike 4.0"
    override val licenseCCBY: String = "Creative Commons BY"

}

object EnglishPracticeImportStrings : PracticeImportStrings {

    override val title: String = "Select"

    override val kanaTitle: String = "Kana"

    override val kanaDescription = { urlColor: Color ->
        buildAnnotatedString {
            append("Kana is the most basic japanese writing system, which consist of 2 alphabets: hiragana - used for native Japanese words and grammatical elements, and katakana that represents foreign words. ")
            withClickableUrl(
                url = "https://en.wikipedia.org/wiki/Kana",
                color = urlColor
            ) {
                append("More info.")
            }
        }
    }
    override val hiragana: String = "Hiragana"
    override val katakana: String = "Katakana"

    override val jltpTitle: String = "JLPT"
    override val jlptDescription = { urlColor: Color ->
        buildAnnotatedString {
            append("Kana is the most basic japanese writing system, which consist of 2 alphabets: hiragana - used for native Japanese words and grammatical elements, and katakana that represents foreign words. ")
            withClickableUrl("https://en.wikipedia.org/wiki/Kana", urlColor) {
                append("More info.")
            }
        }
    }
    override val jlptItem: (level: Int) -> String = { "JLPT・N$it" }

    override val gradeTitle: String = "Grade"
    override val gradeDescription = { urlColor: Color ->
        buildAnnotatedString {
            withClickableUrl("https://en.wikipedia.org/wiki/J%C5%8Dy%C5%8D_kanji", urlColor) {
                append("The Jōyō kanji")
            }
            append(" is a list of 2,136 frequently used characters maintained officially by the Japanese Ministry of Education. ")
            append("All these characters are taught in Japanese schools:\n")
            append(" • 1,026 kanji taught in primary school (Grade 1-6) (the ")
            withClickableUrl("https://en.wikipedia.org/wiki/Ky%C5%8Diku_kanji", urlColor) {
                append("kyōiku kanji")
            }
            append(")\n")
            append(" • 1,110 additional kanji taught in secondary school (Grade 7-12)")
        }
    }
    override val gradeItemNumbered: (Int) -> String = { "Grade $it" }
    override val gradeItemSecondary: String = "Secondary school"
    override val gradeItemNames: String = "Kanji for use in names (Jinmeiyō)"
    override val gradeItemNamesVariants: String = "Jinmeiyō kanji variants of Jōyō"

    override val wanikaniTitle: String = "Wanikani"
    override val wanikaniDescription = { urlColor: Color ->
        buildAnnotatedString {
            append("Kanji lists according to levels on website Wanikani by Tofugu. ")
            withClickableUrl("https://www.wanikani.com/kanji?difficulty=pleasant", urlColor) {
                append("More info. ")
            }
        }
    }
    override val wanikaniItem: (Int) -> String = { "Wanikani Level $it" }

}