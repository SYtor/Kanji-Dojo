package ua.syt0r.kanji.presentation.screen.main.screen.deck_picker.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import kotlinx.serialization.Serializable
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

data class DeckPickerDeck(
    val previewText: String,
    val title: @Composable () -> String,
    val classification: CharacterClassification
)
