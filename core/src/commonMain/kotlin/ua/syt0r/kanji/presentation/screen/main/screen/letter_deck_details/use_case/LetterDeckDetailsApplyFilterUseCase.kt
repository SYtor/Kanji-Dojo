package ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.use_case

import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.CharacterReviewState
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.FilterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.LetterDeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.letter_deck_details.data.PracticeType

interface LetterDeckDetailsApplyFilterUseCase {

    operator fun invoke(
        items: List<LetterDeckDetailsItemData>,
        practiceType: PracticeType,
        filterConfiguration: FilterConfiguration,
    ): List<LetterDeckDetailsItemData>

}

class DefaultLetterDeckDetailsApplyFilterUseCase : LetterDeckDetailsApplyFilterUseCase {

    override fun invoke(
        items: List<LetterDeckDetailsItemData>,
        practiceType: PracticeType,
        filterConfiguration: FilterConfiguration,
    ): List<LetterDeckDetailsItemData> {
        return items.filter {
            val reviewState = when (practiceType) {
                PracticeType.Writing -> it.writingSummary.state
                PracticeType.Reading -> it.readingSummary.state
            }
            when (reviewState) {
                CharacterReviewState.New -> filterConfiguration.showNew
                CharacterReviewState.Due -> filterConfiguration.showDue
                CharacterReviewState.Done -> filterConfiguration.showDone
            }
        }
    }

}
