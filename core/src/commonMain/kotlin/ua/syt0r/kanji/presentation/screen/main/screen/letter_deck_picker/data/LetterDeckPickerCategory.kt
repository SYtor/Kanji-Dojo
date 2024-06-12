package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_picker.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.common.theme.extraColorScheme

@Serializable
sealed interface LetterDeckPickerCategory {

    val titleResolver: StringResolveScope<String>

    @get:Composable
    val descriptionResolver: StringResolveScope<AnnotatedString>

    val items: List<LetterDeckPickerItem>

}

@Serializable
object KanaLetterDeckPickerCategory : LetterDeckPickerCategory {

    override val titleResolver: StringResolveScope<String> = { letterDeckPicker.kanaTitle }

    override val descriptionResolver: StringResolveScope<AnnotatedString>
        @Composable
        get() {
            val color = MaterialTheme.extraColorScheme.link
            return { letterDeckPicker.kanaDescription(color) }
        }

    override val items: List<LetterDeckPickerItem> = listOf(HiraganaImportItem, KatakanaImportItem)

}

@Serializable
object JlptLetterDeckPickerCategory : LetterDeckPickerCategory {

    override val titleResolver: StringResolveScope<String> = { letterDeckPicker.jltpTitle }

    override val descriptionResolver: StringResolveScope<AnnotatedString>
        @Composable
        get() {
            val color = MaterialTheme.extraColorScheme.link
            return { letterDeckPicker.jlptDescription(color) }
        }

    override val items: List<LetterDeckPickerItem> = JlptImportItems

}

@Serializable
object GradeLetterDeckPickerCategory : LetterDeckPickerCategory {

    override val titleResolver: StringResolveScope<String> = { letterDeckPicker.gradeTitle }

    override val descriptionResolver: StringResolveScope<AnnotatedString>
        @Composable
        get() {
            val color = MaterialTheme.extraColorScheme.link
            return { letterDeckPicker.gradeDescription(color) }
        }

    override val items: List<LetterDeckPickerItem> = GradeImportItems

}

@Serializable
object WanikaniImportCategory : LetterDeckPickerCategory {

    override val titleResolver: StringResolveScope<String> = { letterDeckPicker.wanikaniTitle }

    override val descriptionResolver: StringResolveScope<AnnotatedString>
        @Composable
        get() {
            val color = MaterialTheme.extraColorScheme.link
            return { letterDeckPicker.wanikaniDescription(color) }
        }

    override val items: List<LetterDeckPickerItem> = WanikaniImportItems

}

val AllImportCategories = listOf(
    KanaLetterDeckPickerCategory,
    JlptLetterDeckPickerCategory,
    GradeLetterDeckPickerCategory,
    WanikaniImportCategory
)