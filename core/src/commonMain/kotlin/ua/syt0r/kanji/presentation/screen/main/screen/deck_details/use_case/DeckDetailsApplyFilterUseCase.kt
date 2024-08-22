package ua.syt0r.kanji.presentation.screen.main.screen.deck_details.use_case

import ua.syt0r.kanji.core.srs.SrsItemStatus
import ua.syt0r.kanji.presentation.common.ScreenLetterPracticeType
import ua.syt0r.kanji.presentation.common.ScreenVocabPracticeType
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.DeckDetailsItemData
import ua.syt0r.kanji.presentation.screen.main.screen.deck_details.data.FilterConfiguration

interface DeckDetailsApplyFilterUseCase {

    operator fun invoke(
        items: List<DeckDetailsItemData.LetterData>,
        practiceType: ScreenLetterPracticeType,
        filterConfiguration: FilterConfiguration,
    ): List<DeckDetailsItemData.LetterData>

    operator fun invoke(
        items: List<DeckDetailsItemData.VocabData>,
        practiceType: ScreenVocabPracticeType,
        filterConfiguration: FilterConfiguration,
    ): List<DeckDetailsItemData.VocabData>

}

class DefaultDeckDetailsApplyFilterUseCase : DeckDetailsApplyFilterUseCase {

    override fun invoke(
        items: List<DeckDetailsItemData.LetterData>,
        practiceType: ScreenLetterPracticeType,
        filterConfiguration: FilterConfiguration,
    ): List<DeckDetailsItemData.LetterData> {
        return items.filter {
            val reviewState = when (practiceType) {
                ScreenLetterPracticeType.Writing -> it.writingSummary.srsItemStatus
                ScreenLetterPracticeType.Reading -> it.readingSummary.srsItemStatus
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
        practiceType: ScreenVocabPracticeType,
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
