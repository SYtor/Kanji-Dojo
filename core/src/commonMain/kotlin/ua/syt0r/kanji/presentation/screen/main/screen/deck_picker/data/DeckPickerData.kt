package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data

import androidx.compose.ui.text.AnnotatedString
import kotlinx.serialization.Serializable
import ua.syt0r.kanji.core.app_data.WordClassification
import ua.syt0r.kanji.core.japanese.CharacterClassification
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope


@Serializable
sealed interface DeckPickerScreenConfiguration {

    @Serializable
    object Letters : DeckPickerScreenConfiguration

    @Serializable
    object Vocab : DeckPickerScreenConfiguration

}

data class DeckPickerCategory(
    val title: StringResolveScope<String>,
    val description: StringResolveScope<AnnotatedString>,
    val items: List<DeckPickerDeck>
)

sealed interface DeckPickerDeck {
    val title: StringResolveScope<String>
}

data class LetterDeckPickerDeck(
    override val title: StringResolveScope<String>,
    val previewText: String,
    val classification: CharacterClassification
) : DeckPickerDeck

data class VocabDeckPickerDeck(
    override val title: StringResolveScope<String>,
    val classification: WordClassification,
    val wordsCount: Int
) : DeckPickerDeck
