package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import ua.syt0r.kanji.presentation.common.resources.string.StringResolveScope


data class LetterDeckDetailsConfiguration(
    val practiceType: PracticeType,
    val filterConfiguration: FilterConfiguration,
    val sortOption: SortOption,
    val isDescending: Boolean,
    val layout: DeckDetailsLayout,
    val kanaGroups: Boolean,
)

typealias RepoPracticeType = ua.syt0r.kanji.core.user_data.preferences.PracticeType
typealias RepoSortOption = ua.syt0r.kanji.core.user_data.preferences.SortOption
typealias RepoLayout = ua.syt0r.kanji.core.user_data.preferences.PracticePreviewLayout

enum class PracticeType(
    val titleResolver: StringResolveScope<String>,
    val correspondingRepoType: RepoPracticeType,
    val imageVector: ImageVector,
) {
    Writing(
        titleResolver = { practicePreview.practiceType.practiceTypeWriting },
        correspondingRepoType = RepoPracticeType.Writing,
        imageVector = Icons.Default.Draw
    ),
    Reading(
        titleResolver = { practicePreview.practiceType.practiceTypeReading },
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

enum class SortOption(
    val titleResolver: StringResolveScope<String>,
    val hintResolver: StringResolveScope<String>,
    val correspondingRepoType: RepoSortOption,
) {
    ADD_ORDER(
        titleResolver = { practicePreview.sortDialog.sortOptionAddOrder },
        hintResolver = { practicePreview.sortDialog.sortOptionAddOrderHint },
        correspondingRepoType = RepoSortOption.AddOrder
    ),
    FREQUENCY(
        titleResolver = { practicePreview.sortDialog.sortOptionFrequency },
        hintResolver = { practicePreview.sortDialog.sortOptionFrequencyHint },
        correspondingRepoType = RepoSortOption.Frequency
    ),
    NAME(
        titleResolver = { practicePreview.sortDialog.sortOptionName },
        hintResolver = { practicePreview.sortDialog.sortOptionNameHint },
        correspondingRepoType = RepoSortOption.Name
    );

    val imageVector = Icons.AutoMirrored.Filled.ArrowForward
}

fun RepoSortOption.toScreenType() = SortOption.values().find { it.correspondingRepoType == this }!!


enum class DeckDetailsLayout(
    val titleResolver: StringResolveScope<String>,
    val correspondingRepoType: RepoLayout,
) {
    SingleCharacter(
        titleResolver = { practicePreview.layoutDialog.singleCharacterOptionLabel },
        correspondingRepoType = RepoLayout.Character
    ),
    Groups(
        titleResolver = { practicePreview.layoutDialog.groupsOptionLabel },
        correspondingRepoType = RepoLayout.Groups
    )
}

fun RepoLayout.toScreenType() = DeckDetailsLayout.values()
    .find { it.correspondingRepoType == this }!!
