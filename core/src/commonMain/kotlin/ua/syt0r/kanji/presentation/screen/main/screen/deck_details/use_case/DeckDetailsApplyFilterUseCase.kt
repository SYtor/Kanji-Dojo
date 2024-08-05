package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case

import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.FilterConfiguration
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.PracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.practice_vocab.data.VocabPracticeType

interface DeckDetailsApplyFilterUseCase {

    operator fun invoke(
        items: List<DeckDetailsItemData.LetterData>,
        practiceType: PracticeType,
        filterConfiguration: FilterConfiguration,
    ): List<DeckDetailsItemData.LetterData>

    operator fun invoke(
        items: List<DeckDetailsItemData.VocabData>,
        practiceType: VocabPracticeType,
        filterConfiguration: FilterConfiguration,
    ): List<DeckDetailsItemData.VocabData>

}

class DefaultDeckDetailsApplyFilterUseCase : DeckDetailsApplyFilterUseCase {

    override fun invoke(
        items: List<DeckDetailsItemData.LetterData>,
        practiceType: PracticeType,
        filterConfiguration: FilterConfiguration,
    ): List<DeckDetailsItemData.LetterData> {
        return items.filter {
            val reviewState = when (practiceType) {
                PracticeType.Writing -> it.writingSummary.srsItemStatus
                PracticeType.Reading -> it.readingSummary.srsItemStatus
            }
            when (reviewState) {
                SrsItemStatus.New -> filterConfiguration.showNew
                SrsItemStatus.Review -> filterConfiguration.showDue
                SrsItemStatus.Done -> filterConfiguration.showDone
            }
        }
    }

    override fun invoke(
        items: List<DeckDetailsItemData.VocabData>,
        practiceType: VocabPracticeType,
        filterConfiguration: FilterConfiguration
    ): List<DeckDetailsItemData.VocabData> {
        return items.filter {
            val reviewState = it.srsStatus.getValue(practiceType)
            when (reviewState) {
                SrsItemStatus.New -> filterConfiguration.showNew
                SrsItemStatus.Review -> filterConfiguration.showDue
                SrsItemStatus.Done -> filterConfiguration.showDone
            }
        }
    }

}
