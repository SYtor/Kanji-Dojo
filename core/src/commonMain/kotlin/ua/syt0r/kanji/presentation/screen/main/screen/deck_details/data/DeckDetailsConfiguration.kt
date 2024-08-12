package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import ua.syt0r.kanji.core.user_data.preferences.PreferencesDeckDetailsLetterLayout
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterPracticeType
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterSortOption
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType


sealed interface DeckDetailsConfiguration {

    data class LetterDeckConfiguration(
        val practiceType: PracticeType,
        val filterConfiguration: FilterConfiguration,
        val sortOption: LettersSortOption,
        val isDescending: Boolean,
        val layout: DeckDetailsLayout,
        val kanaGroups: Boolean,
    ) : DeckDetailsConfiguration

    data class VocabDeckConfiguration(
        val practiceType: VocabPracticeType,
        val filterConfiguration: FilterConfiguration
    ) : DeckDetailsConfiguration

}

enum class PracticeType(
    val titleResolver: StringResolveScope<String>,
    val preferencesType: PreferencesLetterPracticeType,
    val imageVector: ImageVector,
) {
    Writing(
        titleResolver = { deckDetails.practiceType.practiceTypeWriting },
        preferencesType = PreferencesLetterPracticeType.Writing,
        imageVector = Icons.Default.Draw
    ),
    Reading(
        titleResolver = { deckDetails.practiceType.practiceTypeReading },
        preferencesType = PreferencesLetterPracticeType.Reading,
        imageVector = Icons.Default.LocalLibrary
    )
}

fun PreferencesLetterPracticeType.toScreenType() =
    PracticeType.values().first { it.preferencesType == this }

data class FilterConfiguration(
    val showNew: Boolean,
    val showDue: Boolean,
    val showDone: Boolean,
)

enum class LettersSortOption(
    val titleResolver: StringResolveScope<String>,
    val hintResolver: StringResolveScope<String>,
    val preferencesType: PreferencesLetterSortOption,
) {
    ADD_ORDER(
        titleResolver = { deckDetails.sortDialog.sortOptionAddOrder },
        hintResolver = { deckDetails.sortDialog.sortOptionAddOrderHint },
        preferencesType = PreferencesLetterSortOption.AddOrder
    ),
    FREQUENCY(
        titleResolver = { deckDetails.sortDialog.sortOptionFrequency },
        hintResolver = { deckDetails.sortDialog.sortOptionFrequencyHint },
        preferencesType = PreferencesLetterSortOption.Frequency
    ),
    NAME(
        titleResolver = { deckDetails.sortDialog.sortOptionName },
        hintResolver = { deckDetails.sortDialog.sortOptionNameHint },
        preferencesType = PreferencesLetterSortOption.Name
    );

    val imageVector = Icons.AutoMirrored.Filled.ArrowForward
}

fun PreferencesLetterSortOption.toScreenType() =
    LettersSortOption.values().find { it.preferencesType == this }!!


enum class DeckDetailsLayout(
    val titleResolver: StringResolveScope<String>,
    val correspondingRepoType: PreferencesDeckDetailsLetterLayout,
) {
    SingleCharacter(
        titleResolver = { deckDetails.layoutDialog.singleCharacterOptionLabel },
        correspondingRepoType = PreferencesDeckDetailsLetterLayout.Character
    ),
    Groups(
        titleResolver = { deckDetails.layoutDialog.groupsOptionLabel },
        correspondingRepoType = PreferencesDeckDetailsLetterLayout.Groups
    )
}

fun PreferencesDeckDetailsLetterLayout.toScreenType() = DeckDetailsLayout.values()
    .find { it.correspondingRepoType == this }!!
