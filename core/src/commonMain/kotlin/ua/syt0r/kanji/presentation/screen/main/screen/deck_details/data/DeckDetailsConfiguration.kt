package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.ui.graphics.vector.ImageVector
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

typealias RepoPracticeType = ua.syt0r.kanji.core.user_data.preferences.PracticeType
typealias RepoSortOption = ua.syt0r.kanji.core.user_data.preferences.SortOption
typealias RepoLayout = ua.syt0r.kanji.core.user_data.preferences.PracticePreviewLayout

enum class PracticeType(
    val titleResolver: StringResolveScope<String>,
    val correspondingRepoType: RepoPracticeType,
    val imageVector: ImageVector,
) {
    Writing(
        titleResolver = { deckDetails.practiceType.practiceTypeWriting },
        correspondingRepoType = RepoPracticeType.Writing,
        imageVector = Icons.Default.Draw
    ),
    Reading(
        titleResolver = { deckDetails.practiceType.practiceTypeReading },
        correspondingRepoType = RepoPracticeType.Reading,
        imageVector = Icons.Default.LocalLibrary
    )
}

fun RepoPracticeType.toScreenType() =
    PracticeType.values().find { it.correspondingRepoType == this }!!

data class FilterConfiguration(
    val showNew: Boolean,
    val showDue: Boolean,
    val showDone: Boolean,
)

enum class LettersSortOption(
    val titleResolver: StringResolveScope<String>,
    val hintResolver: StringResolveScope<String>,
    val correspondingRepoType: RepoSortOption,
) {
    ADD_ORDER(
        titleResolver = { deckDetails.sortDialog.sortOptionAddOrder },
        hintResolver = { deckDetails.sortDialog.sortOptionAddOrderHint },
        correspondingRepoType = RepoSortOption.AddOrder
    ),
    FREQUENCY(
        titleResolver = { deckDetails.sortDialog.sortOptionFrequency },
        hintResolver = { deckDetails.sortDialog.sortOptionFrequencyHint },
        correspondingRepoType = RepoSortOption.Frequency
    ),
    NAME(
        titleResolver = { deckDetails.sortDialog.sortOptionName },
        hintResolver = { deckDetails.sortDialog.sortOptionNameHint },
        correspondingRepoType = RepoSortOption.Name
    );

    val imageVector = Icons.AutoMirrored.Filled.ArrowForward
}

fun RepoSortOption.toScreenType() =
    LettersSortOption.values().find { it.correspondingRepoType == this }!!


enum class DeckDetailsLayout(
    val titleResolver: StringResolveScope<String>,
    val correspondingRepoType: RepoLayout,
) {
    SingleCharacter(
        titleResolver = { deckDetails.layoutDialog.singleCharacterOptionLabel },
        correspondingRepoType = RepoLayout.Character
    ),
    Groups(
        titleResolver = { deckDetails.layoutDialog.groupsOptionLabel },
        correspondingRepoType = RepoLayout.Groups
    )
}

fun RepoLayout.toScreenType() = DeckDetailsLayout.values()
    .find { it.correspondingRepoType == this }!!

enum class VocabSortOption(
    val titleResolver: StringResolveScope<String>,
    val hintResolver: StringResolveScope<String>,
    val correspondingRepoType: RepoSortOption,
) {
    ADD_ORDER(
        titleResolver = { deckDetails.sortDialog.sortOptionAddOrder },
        hintResolver = { deckDetails.sortDialog.sortOptionAddOrderHint },
        correspondingRepoType = RepoSortOption.AddOrder
    );

    val imageVector = Icons.AutoMirrored.Filled.ArrowForward
}
