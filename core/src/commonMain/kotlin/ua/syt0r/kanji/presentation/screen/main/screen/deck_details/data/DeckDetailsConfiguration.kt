package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import ua.syt0r.kanji.core.user_data.preferences.PreferencesDeckDetailsLetterLayout
import ua.syt0r.kanji.core.user_data.preferences.PreferencesLetterSortOption
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope


sealed interface DeckDetailsConfiguration {

    data class LetterDeckConfiguration(
        val practiceType: ScreenLetterPracticeType,
        val filterConfiguration: FilterConfiguration,
        val sortOption: LettersSortOption,
        val isDescending: Boolean,
        val layout: DeckDetailsLayout,
        val kanaGroups: Boolean,
    ) : DeckDetailsConfiguration

    data class VocabDeckConfiguration(
        val practiceType: ScreenVocabPracticeType,
        val filterConfiguration: FilterConfiguration
    ) : DeckDetailsConfiguration

}

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
    ),
    REVIEW_TIME(
        titleResolver = { deckDetails.sortDialog.sortOptionReviewTime },
        hintResolver = { deckDetails.sortDialog.sortOptionReviewTimeHint },
        preferencesType = PreferencesLetterSortOption.ReviewTime
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
